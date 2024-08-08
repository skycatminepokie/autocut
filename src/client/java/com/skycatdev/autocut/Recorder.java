package com.skycatdev.autocut;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class Recorder {
    protected ArrayList<Clip> clips = new ArrayList<>();
    protected ArrayList<RecordingEvent> events = new ArrayList<>();
    protected long startTime;

    public Recorder() {
        startTime = System.currentTimeMillis();
    }

    public void addClip(Clip clip) {
        clips.add(clip);
    }

    public void onRecordingEnded(String outputPath) {
        FFmpegLogCallback.set();
        File recording = new File(outputPath);
        File export = recording.toPath().resolveSibling("cut" + recording.getName()).toFile(); // Successful cut: ffmpeg -i '.\cut2024-08-02 21-46-13.mkv' -vf trim=1:2 -af atrim=1:2 output.mkv
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        File[] filters;
        try {
           filters = buildFilters(clips);
           ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-i", recording.getAbsolutePath(), "-filter_script:v", filters[0].getAbsolutePath(), "-filter_script:a", filters[1].getAbsolutePath(), export.getAbsolutePath());
           pb.inheritIO().start().waitFor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Create filters to keep only the given clips in.
     * @param clips At least one clip. Clips may overlap.
     * @return {@code new File[] {selectFilter, aselectFilter}}
     * @throws IOException
     */
    // Yes the return for the javadoc is exactly the return line, but it doesn't get much clearer than that line.
    public static File[] buildFilters(Collection<Clip> clips) throws IOException { // Find a faster way? Let me know!
        if (clips.isEmpty()) {
            throw new IllegalArgumentException("clips.isEmpty(), cannot build a (meaningful) filter out of no clips.");
        }
        var mergedClips = mergeClips(clips);
        File selectFilter = File.createTempFile("autocutSelectFilter", null);
        selectFilter.deleteOnExit();
        File aselectFilter = File.createTempFile("autocutAselectFilter", null);
        aselectFilter.deleteOnExit();
        try (PrintWriter selectWriter = new PrintWriter(selectFilter); PrintWriter aselectWriter = new PrintWriter(aselectFilter)) {
            String between = mergedClips.getFirst().toBetweenStatement("t");
            selectWriter.print("select='" + between);
            aselectWriter.print("aselect='" + between);
            for (int i = 1; i < mergedClips.size(); i++) {
                between = mergedClips.get(i).toBetweenStatement("t");
                selectWriter.print("+" + between);
                aselectWriter.print("+" + between);
            }
            selectWriter.print("'");
            aselectWriter.print("'");
        }
        return new File[] {selectFilter, aselectFilter};
    }

    /**
     * Sort and merge all overlapping clips together.
     * @param clips The clips to merge together
     * @return A new ArrayList of merged clips.
     */
    private static ArrayList<Clip> mergeClips(Collection<Clip> clips) {
        ArrayList<Clip> mergedClips = new ArrayList<>(clips.stream().map(Clip::copy).sorted(Comparator.comparing(Clip::in)).toList()); // New list so that it's mutable
        int i = 0;
        while (i < mergedClips.size() - 1) { // Don't try to merge the last clip, there's nothing to merge it with
            Clip current = mergedClips.get(i);
            Clip next = mergedClips.get(i + 1);
            if (current.out() <= next.in()) { // If current overlaps next
                Clip newClip = new Clip(current.in(), Math.max(current.out(), next.out()), ClipTypes.INTERNAL, null); // Take the union
                mergedClips.set(i, newClip); // Replace the current
                mergedClips.remove(i + 1); // Yeet the next, it's been combined
                continue; // And check this clip for union with the next
            }
            i ++; // This clip has no overlap, try the next one
        }
        return mergedClips;
    }
}
