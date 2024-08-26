package com.skycatdev.autocut;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class RecordingHandler {
    protected static final Path RECORDING_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("autocut/recordings");
    protected static final String CLIPS_TABLE = "clips";
    protected static final String CLIPS_ID_COLUMN = "id";
    protected static final String CLIPS_INPOINT_COLUMN = "start_timestamp";
    protected static final String CLIPS_TIMESTAMP_COLUMN = "timestamp";
    protected static final String CLIPS_OUTPOINT_COLUMN = "end_timestamp";
    protected static final String CLIPS_TYPE_COLUMN = "type";
    protected static final String CLIPS_DESCRIPTION_COLUMN = "description";

    static {
        RECORDING_DIRECTORY.toFile().mkdirs(); // TODO: Error handling
    }

    protected ArrayList<Clip> clips = new ArrayList<>();
    /**
     * The UNIX time this recorder started.
     */
    protected long startTime;
    protected File database;
    protected String sqlUrl;
    /**
     * Where the video file of the recording is stored. {@code null} when recording has not finished.
     */
    @Nullable protected String outputPath = null;

    public RecordingHandler() throws SQLException, IOException {
        startTime = System.currentTimeMillis();
        database = RECORDING_DIRECTORY.resolve("autocut_" + startTime + ".sqlite").toFile();
        database.createNewFile(); // TODO: Handle duplicate files
        sqlUrl = "jdbc:sqlite:" + database.getPath();
        try (Connection connection = DriverManager.getConnection(sqlUrl); Statement statement = connection.createStatement()) {
            statement.execute(String.format("""
                    CREATE TABLE %s (
                        %s INTEGER PRIMARY KEY AUTOINCREMENT,
                        %s INTEGER,
                        %s INTEGER,
                        %s INTEGER,
                        %s TEXT,
                        %s TEXT
                    );""",
                    CLIPS_TABLE,
                    CLIPS_ID_COLUMN,
                    CLIPS_INPOINT_COLUMN,
                    CLIPS_TIMESTAMP_COLUMN,
                    CLIPS_OUTPOINT_COLUMN,
                    CLIPS_TYPE_COLUMN,
                    CLIPS_DESCRIPTION_COLUMN));
        }
    }

    /**
     * Sort and merge all overlapping clips together.
     *
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
            i++; // This clip has no overlap, try the next one
        }
        return mergedClips;
    }

    /**
     * Adds a new clip to the recording.
     *
     * @param clip The clip to add.
     */
    public void addClip(Clip clip) throws SQLException {
        clips.add(clip);
        try (Connection connection = DriverManager.getConnection(sqlUrl);
             PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?);",
                     CLIPS_TABLE,
                     CLIPS_INPOINT_COLUMN,
                     CLIPS_OUTPOINT_COLUMN,
                     CLIPS_TYPE_COLUMN,
                     CLIPS_DESCRIPTION_COLUMN))) {
            statement.setLong(1, clip.in());
            statement.setLong(2, clip.out());
            statement.setString(3, clip.type().toString());
            statement.setString(4, clip.description());
            statement.execute();
        }
    }

    /**
     * Builds a filter that keeps and concatenates only the clips given.
     *
     * @param clips The clips to keep. Must not be empty.
     * @return A new temporary file containing the filter
     * @throws IOException If there's problems with the file
     */
    public File buildComplexFilter(Collection<Clip> clips) throws IOException { // TODO: currently only handles one audio track
        if (clips.isEmpty()) {
            throw new IllegalArgumentException("clips.isEmpty(), cannot build a (meaningful) filter out of no clips.");
        }
        var mergedClips = mergeClips(clips);
        File filter = File.createTempFile("autocutComplexFilter", null);
        filter.deleteOnExit();
        try (PrintWriter pw = new PrintWriter(filter)) {

            String between = mergedClips.getFirst().toBetweenStatement("t", startTime);
            if (mergedClips.size() == 1) {
                pw.printf("[0:v]select=%s,setpts=PTS-STARTPTS[outv];[0:a]aselect=%s,asetpts=PTS-STARTPTS[outa]", between, between);
            } else {
                // First clip
                String videoIn = "[0:v]";
                String audioIn = "[0:a]";
                pw.printf("%sselect=%s,setpts=PTS-STARTPTS%s;", videoIn, between, "[0v]");
                pw.printf("%saselect=%s,asetpts=PTS-STARTPTS%s", audioIn, between, "[0a]");

                // Clips 2 thru n
                for (int i = 1; i < mergedClips.size(); i++) {
                    between = mergedClips.get(i).toBetweenStatement("t", startTime);
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
     * Export all clips in the recording with ffmpeg. {@link RecordingHandler#outputPath} must not be {@code null}.
     */
    public void export(String ffmpeg) {
        if (outputPath == null) {
            throw new IllegalStateException("outputPath was null and it must not be. Has the recording finished/onRecordingEnded been called?");
        }
        new Thread(() -> {
            File recording = new File(outputPath);
            File export = recording.toPath().resolveSibling("cut" + recording.getName()).toFile();
            try {
                ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-/filter_complex", buildComplexFilter(clips).getAbsolutePath(), "-i", recording.getAbsolutePath(), "-map", "[outv]", "-map", "[outa]", "-codec:v", "libx264", "-crf", "18", export.getAbsolutePath()); // WARN: Requires a build of ffmpeg that supports libx264
                pb.inheritIO().start().waitFor();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * @return how long this has been recording, in milliseconds
     */
    public long getRecordingTime() {
        return System.currentTimeMillis() - startTime;
    }

    public void onRecordingEnded(String outputPath) {
        this.outputPath = outputPath;
    }
}
