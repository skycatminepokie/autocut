package com.skycatdev.autocut.export;

import com.google.common.collect.Range;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.Utils;
import com.skycatdev.autocut.clips.Clip;
import com.skycatdev.autocut.clips.ClipType;
import com.skycatdev.autocut.clips.ClipTypes;
import com.skycatdev.autocut.config.ConfigHandler;
import com.skycatdev.autocut.record.RecordingManager;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExportHelper {
    /**
     * Export all clips in the record with ffmpeg. {@link RecordingManager#outputPath} must not be {@code null}.
     *
     * @param clips         All the clips to be considered when exporting. Don't include inactive clips.
     * @param recordingPath The path to the video recording
     * @param startTime     The time the recording was started
     */
    public static void export(LinkedList<Clip> clips, @NotNull String recordingPath, long startTime) {
        new Thread(() -> {
            File recording = new File(recordingPath);

            try {
                FFmpegExecutor executor = new FFmpegExecutor();
                FFprobe ffprobe = new FFprobe();
                FFmpegProbeResult in = ffprobe.probe(recordingPath);

                // Split clips into export sets
                HashMap<ClipType, LinkedList<Clip>> typedExportSets = new HashMap<>();
                LinkedList<Clip> mainSet = new LinkedList<>();
                LinkedList<Clip> individualSet = new LinkedList<>();

                for (Clip clip : clips) {
                    ClipType clipType = Objects.requireNonNull(ClipTypes.CLIP_TYPE_REGISTRY.get(clip.type())).clipType();
                    switch (clipType.getExportGroupingMode()) {
                        case NONE -> mainSet.add(clip);
                        case TYPE -> typedExportSets.computeIfAbsent(clipType, k -> new LinkedList<>()).add(clip);
                        case INDIVIDUAL -> individualSet.add(clip);
                        default -> throw new IllegalArgumentException("Export grouping mode was unrecognized: " + clipType.getExportGroupingMode());
                    }
                }
                int totalJobs = (mainSet.isEmpty() ? 0 : 1) + individualSet.size() + typedExportSets.size();
                int jobIndex = 0;

                AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.start"));

                try {
                    // For each set create a job
                    if (!mainSet.isEmpty()) {
                        makeFFmpegJob(mainSet, startTime, recording, in, executor, totalJobs, ++jobIndex).run();
                    }
                    for (LinkedList<Clip> typedExportClips : typedExportSets.values()) {
                        makeFFmpegJob(typedExportClips, startTime, recording, in, executor, totalJobs, ++jobIndex).run();
                    }
                    for (Clip clip : individualSet) {
                        LinkedList<Clip> dummyList = new LinkedList<>();
                        dummyList.add(clip);
                        makeFFmpegJob(dummyList, startTime, recording, in, executor, totalJobs, ++jobIndex).run();
                    }

                } catch (Exception e) {
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.progress.fail"));
                    throw new RuntimeException("Something went wrong while exporting.", e);
                }
            } catch (Exception e) {
                AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.fail"));
                throw new RuntimeException(e);
            }
        }, "Autocut FFmpeg Export Thread").start();
    }

    private static FFmpegJob makeFFmpegJob(LinkedList<Clip> clips, long startTime, File recording, FFmpegProbeResult in, FFmpegExecutor executor, int totalJobs, int jobNumber) throws IOException {
        Set<Range<Long>> rangeSet = Clip.toRange(clips).asRanges();
        File export = ConfigHandler.getExportConfig().getExportFile(recording, rangeSet.size());
        @SuppressWarnings("SpellCheckingInspection") FFmpegBuilder builder = new FFmpegBuilder()
                .addExtraArgs("-/filter_complex", FilterGenerator.buildComplexFilter(startTime, in, rangeSet).getAbsolutePath())
                .setInput(in)
                .addOutput(export.getAbsolutePath())
                .setFormat(ConfigHandler.getExportConfig().getFormat())
                .addExtraArgs("-map", "[outv]", "-map", "[outa]")
                .setConstantRateFactor(18)
                //.setVideoCodec("libx264") requires gpl
                .done();
        FFmpegJob job = executor.createJob(builder, new ProgressListener() {
            final long outputDurationNs = TimeUnit.MILLISECONDS.toNanos(Utils.totalSpace(rangeSet));
            int lastPercentDone = -1;
            long lastMessageTime = 0;

            @Override
            public void progress(Progress progress) {
                if (progress.isEnd()) {
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.finish", jobNumber, totalJobs));
                } else {
                    int percentDone = (int)(((double) progress.out_time_ns / outputDurationNs) * 100);
                    if (percentDone < 0 || percentDone == lastPercentDone && System.currentTimeMillis() < lastMessageTime + 10000) { // If it's strange or (it's the same number and it's been less than 10 secs)
                        return;
                    }
                    lastPercentDone = percentDone;
                    lastMessageTime = System.currentTimeMillis();
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.progress", percentDone, jobNumber, totalJobs));
                }

            }
        });
        return job;
    }
}
