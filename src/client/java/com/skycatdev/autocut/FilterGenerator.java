package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.Clip;
import com.skycatdev.autocut.clips.ClipBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.probe.FFmpegStream;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class FilterGenerator {
    /**
     * Creates a single {@code select} filter for a single audio stream.
     *
     * @param input  The command input index of the filter's input
     * @param stream The index of the stream from the input
     * @param range  The {@code range} statement
     * @param clipIndex The clip index for this particular stream
     * @return a {@code select} filter, with output in the form {@code "[{stream}a{clipIndex}]"}
     */
    private static String audioFilter(int input, int stream, String range, int clipIndex) {
        return String.format("[%d:a:%d]atrim=%s,asetpts=PTS-STARTPTS[%da%d]", input, stream, range, stream, clipIndex);
    }

    /**
     * Builds a filter that keeps and concatenates only the clips given.
     *
     * @param startTime   The time the recording started
     * @param clips       The clips to keep. Must not be empty.
     * @param probeResult The result from probing the raw recording file. Must not have errors, and must have at least one video or audio stream.
     * @return A new temporary file containing the filter
     * @throws IOException If there's problems with the file
     */
    protected static File buildComplexFilter(long startTime, Collection<Clip> clips, FFmpegProbeResult probeResult) throws IOException {
        if (clips.isEmpty()) {
            throw new IllegalArgumentException("clips.isEmpty(), cannot build a (meaningful) filter out of no clips.");
        }
        if (probeResult.hasError()) {
            throw new IllegalArgumentException("probeResult.hasError(), and it must not.");
        }

        // Decide what streams we have
        int videoStreams = 0;
        int audioStreams = 0;
        for (FFmpegStream stream : probeResult.getStreams()) {
            if (stream.codec_type.equals(FFmpegStream.CodecType.AUDIO)) {
                audioStreams++;
                continue;
            }
            if (stream.codec_type.equals(FFmpegStream.CodecType.VIDEO)) {
                videoStreams++;
            }
        }

        // Prep for writing
        var mergedClips = mergeClips(clips);
        File filter = File.createTempFile("autocutComplexFilter", null);
        filter.deleteOnExit();

        try (PrintWriter pw = new PrintWriter(filter)) {

            String range = mergedClips.getFirst().toTrimRange(startTime);
            int numberOfClips = mergedClips.size();
            writeFirstClip(videoStreams, pw, range, audioStreams);
            if (numberOfClips != 1) { // Multiple clips
                for (int i = 1; i < numberOfClips; i++) { // For each clip
                    range = mergedClips.get(i).toTrimRange(startTime);
                    for (int v = 0; v < videoStreams; v++) { // For each video stream
                        pw.print(';');
                        pw.print(videoFilter(0, v, range, i));
                    }
                    for (int a = 0; a < audioStreams; a++) { // For each audio stream
                        pw.print(';');
                        pw.print(audioFilter(0, a, range, i));
                    }
                }
            }
            writeConcatAllClips(videoStreams, pw, numberOfClips, audioStreams);
        }
        return filter;
    }

    private static void writeFirstClip(int videoStreams, PrintWriter pw, String range, int audioStreams) {
        // Why is it written like this? Because we need to avoid both a leading and a trailing ';', so we need this logic for starting it properly.
        if (videoStreams > 0) { // At least one video stream
            pw.print(videoFilter(0, 0, range, 0));
            for (int v = 1; v < videoStreams; v++) {
                pw.print(';');
                pw.print(videoFilter(0, v, range, 0));
            }
            for (int a = 0; a < audioStreams; a++) {
                pw.print(';');
                pw.print(audioFilter(0, a, range, 0));
            }
        } else { // No video, audio is going to be our first
            if (audioStreams > 0) {
                pw.print(audioFilter(0, 0, range, 0));
                for (int a = 1; a < audioStreams; a++) {
                    pw.print(';');
                    pw.print(audioFilter(0, a, range, 0));
                }
            } else {
                throw new IllegalArgumentException("Can't handle zero audio and zero video streams.");
            }
        }
    }

    private static void writeConcatAllClips(int videoStreams, PrintWriter pw, int clips, int audioStreams) {
        // Concat
        // Video streams
        for (int v = 0; v < videoStreams; v++) {
            pw.print(';');
            // List inputs
            for (int i = 0; i < clips; i++) {
                pw.printf("[%dv%d]", v, i);
            }
            pw.printf("concat=n=%d:v=1:a=0[outv%d]", clips, v);
        }
        // Audio streams
        for (int a = 0; a < audioStreams; a++) {
            pw.print(';');
            // List inputs
            for (int i = 0; i < clips; i++) {
                pw.printf("[%da%d]", a, i);
            }
            pw.printf("concat=n=%d:v=0:a=1[outa%d]", clips, a);
        }
        // Video streams and audio streams together
        pw.printf(";");
        for (int v = 0; v < videoStreams; v++) {
            pw.printf("[outv%d]", v);
        }
        for (int a = 0; a < audioStreams; a++) {
            pw.printf("[outa%d]", a);
        }
        pw.printf("concat=n=1:v=%d:a=%d[outv][outa]", videoStreams, audioStreams);
    }

    /**
     * Sort and merge all overlapping clips together.
     *
     * @param clips The clips to merge together.
     * @return A new ArrayList of merged clips.
     */
    protected static ArrayList<Clip> mergeClips(Collection<Clip> clips) {
        ArrayList<Clip> mergedClips = new ArrayList<Clip>(clips.stream().map(Clip::copy).sorted(Comparator.comparing(Clip::in)).toList()); // New list so that it's mutable
        int i = 0;
        while (i < mergedClips.size() - 1) { // Don't try to merge the last clip, there's nothing to merge it with
            Clip current = mergedClips.get(i);
            Clip next = mergedClips.get(i + 1);
            if (next.in() <= current.out()) { // If current overlaps next
                Clip newClip = new ClipBuilder(current.in(), Math.min(current.out(), next.out()), Math.max(current.out(), next.out()), RecordingManager.INTERNAL).build(); // Take the union
                mergedClips.set(i, newClip); // Replace the current
                mergedClips.remove(i + 1); // Yeet the next, it's been combined
                continue; // And check this clip for union with the next
            }
            i++; // This clip has no overlap, try the next one
        }
        return mergedClips;
    }

    /**
     * Creates a single {@code select} filter for a single video stream.
     *
     * @param input  The command input index of the filter's input
     * @param stream The index of the stream from the input
     * @param range  The {@code range} statement
     * @param clipIndex The clip index for this particular stream
     * @return a {@code select} filter, with output in the form {@code "[{stream}v{clipIndex}]"}
     */
    private static String videoFilter(int input, int stream, String range, int clipIndex) {
        return String.format("[%d:v:%d]trim=%s,setpts=PTS-STARTPTS[%dv%d]", input, stream, range, stream, clipIndex);
    }
}