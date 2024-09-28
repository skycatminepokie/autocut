package com.skycatdev.autocut.export;

import com.google.common.collect.Range;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.Utils;
import com.skycatdev.autocut.clips.Clip;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExportHelper {
    /**
     * Export all clips in the record with ffmpeg. {@link RecordingManager#outputPath} must not be {@code null}.
     *
     * @param clips All the clips to be considered when exporting. Don't include inactive clips.
     * @param recordingPath The path to the video recording
     * @param startTime The time the recording was started
     */
    public static void export(LinkedList<Clip> clips, @Nullable String recordingPath, long startTime) { // TODO: clean up this error handling, including checking that the recordingPath != null
        if (recordingPath == null) {
            throw new IllegalStateException("recordingPath was null and it must not be. Has the record finished/onRecordingEnded been called?");
        }
        Set<Range<Long>> rangeSet = Clip.toRange(clips).asRanges();
        new Thread(() -> {
            File recording = new File(recordingPath);
            File export = ConfigHandler.getExportConfig().getExportFile(recording, rangeSet.size());

            try {
                FFmpegExecutor executor = new FFmpegExecutor();
                FFprobe ffprobe = new FFprobe();
                FFmpegProbeResult in = ffprobe.probe(recordingPath);

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

                    @Override
                    public void progress(Progress progress) {
                        if (progress.isEnd()) {
                            AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.finish"));
                        } else {
                            double percentDone = ((double) progress.out_time_ns / outputDurationNs) * 100;
                            if (percentDone < 0) {
                                return;
                            }
                            AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.progress", String.format("%.0f", percentDone)));
                        }

                    }
                });
                AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.cutting.start"));
                try {
                    job.run();
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
}
