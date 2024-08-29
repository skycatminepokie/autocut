package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.Clip;
import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.clips.ClipTypes;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RecordingHandler {
    protected static final Path RECORDING_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("autocut/recordings");
    protected static final String CLIPS_TABLE = "clips";
    protected static final String CLIPS_ID_COLUMN = "id";
    protected static final String CLIPS_INPOINT_COLUMN = "start_timestamp";
    protected static final String CLIPS_TIMESTAMP_COLUMN = "timestamp";
    protected static final String CLIPS_OUTPOINT_COLUMN = "end_timestamp";
    protected static final String CLIPS_TYPE_COLUMN = "type";
    protected static final String CLIPS_DESCRIPTION_COLUMN = "description";
    protected static final String CLIPS_SOURCE_COLUMN = "source";
    protected static final String CLIPS_OBJECT_COLUMN = "object";
    protected static final String CLIPS_SOURCE_X_COLUMN = "source_x";
    protected static final String CLIPS_SOURCE_Y_COLUMN = "source_y";
    protected static final String CLIPS_SOURCE_Z_COLUMN = "source_z";
    protected static final String CLIPS_OBJECT_X_COLUMN = "object_x";
    protected static final String CLIPS_OBJECT_Y_COLUMN = "object_y";
    protected static final String CLIPS_OBJECT_Z_COLUMN = "object_z";

    static {
        RECORDING_DIRECTORY.toFile().mkdirs(); // TODO: Error handling
    }

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
                                %s INTEGER NOT NULL,
                                %s INTEGER NOT NULL,
                                %s INTEGER NOT NULL,
                                %s TEXT NOT NULL,
                                %s TEXT,
                                %s TEXT,
                                %s TEXT,
                                %s REAL,
                                %s REAL,
                                %s REAL,
                                %s REAL,
                                %s REAL,
                                %s REAL
                            );""",
                    CLIPS_TABLE,
                    CLIPS_ID_COLUMN,
                    CLIPS_INPOINT_COLUMN,
                    CLIPS_TIMESTAMP_COLUMN,
                    CLIPS_OUTPOINT_COLUMN,
                    CLIPS_TYPE_COLUMN,
                    CLIPS_DESCRIPTION_COLUMN,
                    CLIPS_SOURCE_COLUMN,
                    CLIPS_OBJECT_COLUMN,
                    CLIPS_SOURCE_X_COLUMN,
                    CLIPS_SOURCE_Y_COLUMN,
                    CLIPS_SOURCE_Z_COLUMN,
                    CLIPS_OBJECT_X_COLUMN,
                    CLIPS_OBJECT_Y_COLUMN,
                    CLIPS_OBJECT_Z_COLUMN)
            );
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
                Clip newClip = new ClipBuilder(current.in(), Math.min(current.out(), next.out()), Math.max(current.out(), next.out()), ClipTypes.INTERNAL).build(); // Take the union
                mergedClips.set(i, newClip); // Replace the current
                mergedClips.remove(i + 1); // Yeet the next, it's been combined
                continue; // And check this clip for union with the next
            }
            i++; // This clip has no overlap, try the next one
        }
        return mergedClips;
    }

    protected static PreparedStatement prepareClipStatement(Clip clip, Connection connection) throws SQLException {
        List<Object> rowValues = new LinkedList<>();
        // Required
        StringBuilder columnsBuilder = new StringBuilder("(" + CLIPS_INPOINT_COLUMN + ", " + CLIPS_TIMESTAMP_COLUMN + ", " + CLIPS_OUTPOINT_COLUMN + ", " + CLIPS_TYPE_COLUMN);
        StringBuilder valuesBuilder = new StringBuilder("(?, ?, ?, ?");
        rowValues.add(clip.in());
        rowValues.add(clip.time());
        rowValues.add(clip.out());
        rowValues.add(clip.type());

        // Optional
        if (clip.description() != null) {
            columnsBuilder.append(", " + CLIPS_DESCRIPTION_COLUMN);
            valuesBuilder.append(", ?");
            rowValues.add(clip.description());
        }
        if (clip.source() != null) {
            columnsBuilder.append(", " + CLIPS_SOURCE_COLUMN);
            valuesBuilder.append(", ?");
            rowValues.add(clip.source());
        }
        if (clip.object() != null) {
            columnsBuilder.append(", " + CLIPS_OBJECT_COLUMN);
            valuesBuilder.append(", ?");
            rowValues.add(clip.object());
        }
        if (clip.sourceLocation() != null) {
            columnsBuilder.append(", " + CLIPS_SOURCE_X_COLUMN + ", " + CLIPS_SOURCE_Y_COLUMN + ", " + CLIPS_SOURCE_Z_COLUMN);
            valuesBuilder.append(", ?, ?, ?");
            rowValues.add(clip.sourceLocation().getX());
            rowValues.add(clip.sourceLocation().getY());
            rowValues.add(clip.sourceLocation().getZ());
        }
        if (clip.objectLocation() != null) {
            columnsBuilder.append(", " + CLIPS_OBJECT_X_COLUMN + ", " + CLIPS_OBJECT_Y_COLUMN + ", " + CLIPS_OBJECT_Z_COLUMN);
            valuesBuilder.append(", ?, ?, ?");
            rowValues.add(clip.objectLocation().getX());
            rowValues.add(clip.objectLocation().getY());
            rowValues.add(clip.objectLocation().getZ());
        }

        // Build statement
        columnsBuilder.append(")");
        valuesBuilder.append(")");
        PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s %s VALUES %s", CLIPS_TABLE, columnsBuilder, valuesBuilder));

        // Fill out statement
        for (int i = 1; i <= rowValues.size(); i++) {
            statement.setObject(i, rowValues.get(i - 1));
        }
        return statement;
    }

    /**
     * Adds a new clip to the recording.
     *
     * @param clip The clip to add.
     */
    public void addClip(Clip clip) throws SQLException {
        try (Connection connection = DriverManager.getConnection(sqlUrl); PreparedStatement statement = prepareClipStatement(clip, connection)) {
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
    public void export(String ffmpeg) throws SQLException {
        if (outputPath == null) {
            throw new IllegalStateException("outputPath was null and it must not be. Has the recording finished/onRecordingEnded been called?");
        }
        LinkedList<Clip> clips = getClips();
        new Thread(() -> {
            File recording = new File(outputPath);
            File export = recording.toPath().resolveSibling("cut" + recording.getName()).toFile();

            try {
                FFmpegExecutor executor = new FFmpegExecutor();
                FFprobe ffprobe = new FFprobe();
                FFmpegProbeResult in = ffprobe.probe(outputPath);

                FFmpegBuilder builder = new FFmpegBuilder()
                        .addExtraArgs("-/filter_complex", buildComplexFilter(clips).getAbsolutePath())
                        .setInput(in)
                        .addOutput(export.getAbsolutePath())
                        .addExtraArgs("-map", "[outv]", "-map", "[outa]")
                        .setConstantRateFactor(18)
                        .setVideoCodec("libx264")
                        .done();
                FFmpegJob job = executor.createJob(builder, new ProgressListener() {
                    final double duration_ns = in.getFormat().duration * TimeUnit.SECONDS.toNanos(1);
                    @Override
                    public void progress(Progress progress) {
                        double percentDone = progress.out_time_ns / duration_ns;
                        System.out.printf("%.0f%% done%n", percentDone * 100); // TODO: Make this appear in-game
                    }
                });
                job.run();

                // ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-/filter_complex", buildComplexFilter(clips).getAbsolutePath(), "-i", recording.getAbsolutePath(), "-map", "[outv]", "-map", "[outa]", "-codec:v", "libx264", "-crf", "18", export.getAbsolutePath()); // Requires a build of ffmpeg that supports libx264
                // pb.inheritIO().start().waitFor();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public LinkedList<Clip> getClips() throws SQLException {
        try (Connection connection = DriverManager.getConnection(sqlUrl); Statement statement = connection.createStatement()) {
            ResultSet results = statement.executeQuery("SELECT * FROM " + CLIPS_TABLE + ";");
            LinkedList<Clip> clips = new LinkedList<>();
            while (results.next()) {
                ClipBuilder builder = new ClipBuilder(results.getLong(CLIPS_INPOINT_COLUMN),
                        results.getLong(CLIPS_TIMESTAMP_COLUMN),
                        results.getLong(CLIPS_OUTPOINT_COLUMN),
                        Identifier.of(results.getString(CLIPS_ID_COLUMN)));
                builder.setDescription(CLIPS_DESCRIPTION_COLUMN);
                builder.setSource(CLIPS_SOURCE_COLUMN);
                builder.setObject(CLIPS_OBJECT_COLUMN);
                if (results.getObject(CLIPS_SOURCE_X_COLUMN) instanceof Double x && // instanceof to check for null, cast to avoid re-getting.
                    results.getObject(CLIPS_SOURCE_Y_COLUMN) instanceof Double y &&
                    results.getObject(CLIPS_SOURCE_Z_COLUMN) instanceof Double z) {
                    builder.setSourceLocation(new Vec3d(x, y, z));
                }
                if (results.getObject(CLIPS_OBJECT_X_COLUMN) instanceof Double x &&
                    results.getObject(CLIPS_OBJECT_Y_COLUMN) instanceof Double y &&
                    results.getObject(CLIPS_OBJECT_Z_COLUMN) instanceof Double z) {
                    builder.setObjectLocation(new Vec3d(x, y, z));
                }
                clips.add(builder.build());
            }
            return clips;
        }
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
