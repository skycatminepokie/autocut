package com.skycatdev.autocut;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Recorder {
    protected ArrayList<Clip> clips = new ArrayList<>();
    protected ArrayList<RecordingEvent> events = new ArrayList<>();
    /**
     * The UNIX time this recorder started.
     */
    protected long startTime;
    /**
     * Where the video file of the recording is stored. {@code null} when recording has not finished.
     */
    @Nullable protected String outputPath = null;

    public Recorder() {
        startTime = System.currentTimeMillis();
    }

    /**
     * @return how long this has been recording, in milliseconds
     */
    public long getRecordingTime() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Adds a new clip to the recording.
     * @param clip The clip to add.
     */
    public void addClip(Clip clip) {
        clips.add(clip);
    }

    public void onRecordingEnded(String outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Export all clips in the recording with ffmpeg. {@link Recorder#outputPath} must not be {@code null}.
     */
    public void export() {
        FFmpegLogCallback.set();
        if (outputPath == null) {
            throw new IllegalStateException("outputPath was null and it must not be. Has the recording finished/onRecordingEnded been called?");
        }
        File recording = new File(outputPath);
        File export = recording.toPath().resolveSibling("cut" + recording.getName()).toFile();
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        try {
            ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-codec:v", "libx264", "-i", recording.getAbsolutePath(), "-filter_complex_script", buildComplexFilter(clips).getAbsolutePath(), "-map", "[outv]", "-map", "[outa]", "-crf", "18", export.getAbsolutePath()); // WARN: Requires a build of ffmpeg that supports libx264
            pb.inheritIO().start().waitFor();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Builds a filter that keeps and concatenates only the clips given.
     * @param clips The clips to keep. Must not be empty.
     * @return A new temporary file containing the filter
     * @throws IOException If there's problems with the file
     */
    public static File buildComplexFilter(Collection<Clip> clips) throws IOException { // TODO: currently only handles one audio track
        if (clips.isEmpty()) {
            throw new IllegalArgumentException("clips.isEmpty(), cannot build a (meaningful) filter out of no clips.");
        }
        var mergedClips = mergeClips(clips);
        File filter = File.createTempFile("autocutComplexFilter", null);
        filter.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(filter)) {

            String between = mergedClips.getFirst().toBetweenStatement("t");
            if (mergedClips.size() == 1) {
                pw.printf("[0:v]select=%s[outv];[0:a]aselect=%s[outa]", between, between);
            } else {
                // First clip
                String videoIn = "[0:v]";
                String audioIn = "[0:a]";
                pw.printf("%sselect=%s%s;", videoIn, between, "[0v]");
                pw.printf("%saselect=%s%s", audioIn, between, "[0a]");

                // Clips 2 thru n
                for (int i = 1; i < mergedClips.size(); i++) {
                    between = mergedClips.get(i).toBetweenStatement("t");
                    pw.printf(";%sselect=%s,setpts=PTS-STARTPTS[%dv]", videoIn, between, i);
                    pw.printf(";%saselect=%s,asetpts=PTS-STARTPTS[%da]", audioIn, between, i);
                }

                // Concat
                pw.print(';');
                for (int i = 0; i < mergedClips.size(); i++) { // List all the inputs
                    pw.printf("[%dv][%da]", i, i);
                }
                pw.printf("concat=n=%d:v=1:a=1[outv][outa]", mergedClips.size());
            }
        }
        return filter;
    }

    /**
     * Sort and merge all overlapping clips together.
     * @param clips The clips to merge together.
     * @return A new ArrayList of merged clips.
     */
    private static ArrayList<Clip> mergeClips(Collection<Clip> clips) {
        ArrayList<Clip> mergedClips = new ArrayList<>(clips.stream().map(Clip::copy).sorted(Comparator.comparing(Clip::in)).toList()); // New list so that it's mutable
        int i = 0;
        while (i < mergedClips.size() - 1) { // Don't try to merge the last clip, there's nothing to merge it with
            Clip current = mergedClips.get(i);
            Clip next = mergedClips.get(i + 1);
            if (next.in() <= current.out()) { // If current overlaps next
                Clip newClip = new Clip(current.in(), Math.max(current.out(), next.out()), RecordingElementTypes.INTERNAL, null); // Take the union
                mergedClips.set(i, newClip); // Replace the current
                mergedClips.remove(i + 1); // Yeet the next, it's been combined
                continue; // And check this clip for union with the next
            }
            i ++; // This clip has no overlap, try the next one
        }
        return mergedClips;
    }
}
