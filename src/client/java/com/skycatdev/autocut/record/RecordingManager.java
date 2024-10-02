package com.skycatdev.autocut.record;

import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.clips.Clip;
import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.config.ExportGroupingMode;
import com.skycatdev.autocut.export.ExportHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class RecordingManager {
    protected static final Path RECORDING_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("autocut/recordings");
    protected static final String CLIPS_TABLE = "clips"; // Keep this hardcoded
    protected static final String CLIPS_ID_COLUMN = "id"; // Keep this hardcoded
    protected static final String CLIPS_INPOINT_COLUMN = "start_timestamp"; // Keep this hardcoded
    protected static final String CLIPS_TIMESTAMP_COLUMN = "timestamp"; // Keep this hardcoded
    protected static final String CLIPS_OUTPOINT_COLUMN = "end_timestamp"; // Keep this hardcoded
    protected static final String CLIPS_TYPE_COLUMN = "type"; // Keep this hardcoded
    protected static final String CLIPS_DESCRIPTION_COLUMN = "description"; // Keep this hardcoded
    protected static final String CLIPS_SOURCE_COLUMN = "source"; // Keep this hardcoded
    protected static final String CLIPS_OBJECT_COLUMN = "object"; // Keep this hardcoded
    protected static final String CLIPS_SOURCE_X_COLUMN = "source_x"; // Keep this hardcoded
    protected static final String CLIPS_SOURCE_Y_COLUMN = "source_y"; // Keep this hardcoded
    protected static final String CLIPS_SOURCE_Z_COLUMN = "source_z"; // Keep this hardcoded
    protected static final String CLIPS_OBJECT_X_COLUMN = "object_x"; // Keep this hardcoded
    protected static final String CLIPS_OBJECT_Y_COLUMN = "object_y"; // Keep this hardcoded
    protected static final String CLIPS_OBJECT_Z_COLUMN = "object_z"; // Keep this hardcoded
    protected static final String CLIPS_ACTIVE_COLUMN = "active"; // Keep this hardcoded
    protected static final String META_TABLE = "meta"; // Keep this hardcoded
    protected static final String META_KEY = "key"; // Keep this hardcoded
    protected static final String META_VALUE = "value"; // Keep this hardcoded
    protected static final String META_KEY_START_TIME = "start_time"; // Keep this hardcoded
    protected static final String META_KEY_OUTPUT_PATH = "output_path"; // Keep this hardcoded
    protected static final String CLIPS_INVERSE_COLUMN = "inverse"; // Keep this hardcoded
    protected static final String CLIPS_EXPORT_GROUPING_MODE_COLUMN = "grouping_mode"; // Keep this hardcoded

    static {
        //noinspection ResultOfMethodCallIgnored
        RECORDING_DIRECTORY.toFile().mkdirs();
    }

    /**
     * The UNIX time this recorder started.
     */
    protected long startTime;
    protected @NotNull File database;
    protected @NotNull String sqlUrl;
    /**
     * Where the video file of the record is stored. {@code null} when record has not finished. Probably needs a better name.
     */
    @SuppressWarnings("UnusedAssignment")
    @Nullable
    protected String outputPath = null;

    /**
     * Create a RecordingManager WITHOUT INITIALIZING THE DATABASE.
     *
     * @param startTime  The UNIX time the record started. Make sure it matches the time in the database's meta table.
     * @param database   The database for this record.
     * @param outputPath Where the raw video record is stored. Make sure it matches the path in the database's meta table.
     */
    private RecordingManager(long startTime, @NotNull File database, @Nullable String outputPath) {
        this.startTime = startTime;
        this.database = database;
        this.sqlUrl = "jdbc:sqlite:" + database.getPath();
        this.outputPath = outputPath;
    }

    /**
     * Create a RecordingManager WITHOUT INITIALIZING THE DATABASE.
     *
     * @param startTime The UNIX time the record started. Make sure it matches the time in the database's meta table.
     * @param database  The database for this record.
     */
    private RecordingManager(long startTime, @NotNull File database) {
        this(startTime, database, null);
    }

    /**
     * Create a RecordingManager and initialize its database.
     *
     * @param startTime The UNIX time the record started. Make sure it matches the time in the database's meta table.
     */
    private RecordingManager(long startTime) throws IOException, SQLException {
        this(startTime, RECORDING_DIRECTORY.resolve("autocut_" + Instant.ofEpochMilli(startTime).toString().replace(':', '_').replace('T', '_').replace('.', '_') + ".sqlite").toFile()); // Also initializes sqlUrl
        initializeDatabase(startTime);
    }

    /**
     * Create a RecordingManager starting at this time and initialize its database.
     */
    public RecordingManager() throws SQLException, IOException {
        this(System.currentTimeMillis());
    }

    public static RecordingManager fromDatabase(@NotNull File database) throws SQLException {
        if (!database.exists()) { // This check should prevent connection to a remote server
            throw new IllegalArgumentException("database must exist and it does not.");
        }
        String sqlUrl = "jdbc:sqlite:" + database.getPath();
        long startTime;
        String outputPath;
        // Create connection
        try (Connection connection = DriverManager.getConnection(sqlUrl); Statement statement = connection.createStatement()) { // TODO: prepare this statement
            // Get start time
            ResultSet startTimeResult = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = \"%s\"", META_VALUE, META_TABLE, META_KEY, META_KEY_START_TIME));
            startTimeResult.next();
            startTime = Long.parseLong(startTimeResult.getString(META_VALUE));
            startTimeResult.close();
            // Get record path
            ResultSet recordingPathResult = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = \"%s\"", META_VALUE, META_TABLE, META_KEY, META_KEY_OUTPUT_PATH));
            if (recordingPathResult.next()) {
                outputPath = recordingPathResult.getString(META_VALUE);
            } else {
                outputPath = null;
            }
        }
        return new RecordingManager(startTime, database, outputPath);
    }

    protected static PreparedStatement prepareClipStatement(Clip clip, Connection connection) throws SQLException {
        List<Object> rowValues = new LinkedList<>();
        // Required
        StringBuilder columnsBuilder = new StringBuilder("(%s, %s, %s, %s, %s, %s, %s".formatted(CLIPS_INPOINT_COLUMN,
                CLIPS_TIMESTAMP_COLUMN,
                CLIPS_OUTPOINT_COLUMN,
                CLIPS_TYPE_COLUMN,
                CLIPS_ACTIVE_COLUMN,
                CLIPS_INVERSE_COLUMN,
                CLIPS_EXPORT_GROUPING_MODE_COLUMN));
        StringBuilder valuesBuilder = new StringBuilder("(?, ?, ?, ?, ?, ?, ?");
        rowValues.add(clip.in());
        rowValues.add(clip.time());
        rowValues.add(clip.out());
        rowValues.add(clip.type());
        rowValues.add(clip.active());
        rowValues.add(clip.inverse());
        rowValues.add(clip.exportGroupingMode().getId());

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
        Vec3d sourceLocation = clip.sourceLocation();
        if (sourceLocation != null) {
            columnsBuilder.append(", " + CLIPS_SOURCE_X_COLUMN + ", " + CLIPS_SOURCE_Y_COLUMN + ", " + CLIPS_SOURCE_Z_COLUMN);
            valuesBuilder.append(", ?, ?, ?");
            rowValues.add(sourceLocation.getX());
            rowValues.add(sourceLocation.getY());
            rowValues.add(sourceLocation.getZ());
        }
        Vec3d objectLocation = clip.objectLocation();
        if (objectLocation != null) {
            columnsBuilder.append(", " + CLIPS_OBJECT_X_COLUMN + ", " + CLIPS_OBJECT_Y_COLUMN + ", " + CLIPS_OBJECT_Z_COLUMN);
            valuesBuilder.append(", ?, ?, ?");
            rowValues.add(objectLocation.getX());
            rowValues.add(objectLocation.getY());
            rowValues.add(objectLocation.getZ());
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
     * Adds a new clip to the record.
     *
     * @param clip The clip to add.
     */
    public void addClip(Clip clip) throws SQLException {
        try (Connection connection = DriverManager.getConnection(sqlUrl); PreparedStatement statement = prepareClipStatement(clip, connection)) {
            statement.execute();
        }
    }

    /**
     * Asks {@link com.skycatdev.autocut.export.ExportHelper} to export clips as configured
     * @throws IllegalStateException When {@link outputPath} is {@code null}.
     */
    public void export() throws SQLException {
        if (outputPath == null) {
            throw new IllegalStateException("outputPath is null, and it should not be. Has the recording finished/onRecordingEnded been called?");
        }
        ExportHelper.export(getActiveClips(), outputPath, startTime);

    }

    public LinkedList<Clip> getActiveClips() throws SQLException {
        try (Connection connection = DriverManager.getConnection(sqlUrl); Statement statement = connection.createStatement()) {
            ResultSet results = statement.executeQuery("SELECT * FROM " + CLIPS_TABLE + " WHERE " + CLIPS_ACTIVE_COLUMN + " IS TRUE;");
            LinkedList<Clip> clips = new LinkedList<>();
            while (results.next()) {
                ClipBuilder builder = new ClipBuilder(results.getLong(CLIPS_INPOINT_COLUMN),
                        results.getLong(CLIPS_TIMESTAMP_COLUMN),
                        results.getLong(CLIPS_OUTPOINT_COLUMN),
                        //? if >=1.21
                        Identifier.of(results.getString(CLIPS_TYPE_COLUMN)),
                        //? if <1.21
                        /*Objects.requireNonNull(Identifier.tryParse(results.getString(CLIPS_TYPE_COLUMN))),*/
                        results.getBoolean(CLIPS_ACTIVE_COLUMN),
                        results.getBoolean(CLIPS_INVERSE_COLUMN),
                        ExportGroupingMode.fromId(
                                //? if >=1.21
                                Identifier.of(results.getString(CLIPS_EXPORT_GROUPING_MODE_COLUMN))
                                //? if <1.21
                                /*Objects.requireNonNull(Identifier.tryParse(results.getString(CLIPS_EXPORT_GROUPING_MODE_COLUMN)))*/
                        )
                );
                builder.setDescription(results.getString(CLIPS_DESCRIPTION_COLUMN));
                builder.setSource(results.getString(CLIPS_SOURCE_COLUMN));
                builder.setObject(results.getString(CLIPS_OBJECT_COLUMN));
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
     * @return how long this has been record, in milliseconds
     */
    @SuppressWarnings("unused")
    public long getRecordingTime() {
        return System.currentTimeMillis() - startTime;
    }

    private void initializeDatabase(long startTime) throws IOException, SQLException {
        //noinspection ResultOfMethodCallIgnored
        database.createNewFile();
        try (Connection connection = DriverManager.getConnection(sqlUrl); Statement statement = connection.createStatement()) {
            statement.execute(String.format("""
                            CREATE TABLE %s (
                                %s INTEGER PRIMARY KEY AUTOINCREMENT,
                                %s INTEGER NOT NULL,
                                %s INTEGER NOT NULL,
                                %s INTEGER NOT NULL,
                                %s TEXT NOT NULL,
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
                    CLIPS_ACTIVE_COLUMN,
                    CLIPS_INVERSE_COLUMN,
                    CLIPS_EXPORT_GROUPING_MODE_COLUMN,
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
            statement.execute(String.format("""
                    CREATE TABLE %s (
                        %s TEXT UNIQUE ON CONFLICT FAIL,
                        %s TEXT
                    );""", META_TABLE, META_KEY, META_VALUE));
            try (PreparedStatement insertStatement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?, ?);", META_TABLE))) {
                insertStatement.setString(1, META_KEY_START_TIME);
                insertStatement.setString(2, String.valueOf(startTime));
                insertStatement.execute();
            }
        }
    }

    public void onRecordingEnded(String outputPath) throws SQLException { // Not on client thread
        this.outputPath = outputPath;
        try (Connection connection = DriverManager.getConnection(sqlUrl); PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?, ?);", META_TABLE))) {
            statement.setString(1, META_KEY_OUTPUT_PATH);
            statement.setString(2, outputPath);
            statement.execute();
        }
    }
}
