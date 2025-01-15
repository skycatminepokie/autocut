package com.skycatdev.autocut.database;

import com.skycatdev.autocut.Autocut;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class DatabaseHandler {
    /**
     * The folder where databases are kept in by default.
     */
    public static final Path DATABASE_FOLDER = FabricLoader.getInstance().getGameDir().resolve("autocutRecordings");
    public static final String META = "meta";
    /**
     * The database file. Always exists and is a sqlite database.
     * Do not access the database before first acquiring {@link DatabaseHandler#databaseLock}.
     */
    private final File database;
    private final Object[] databaseLock = new Object[0];
    /**
     * Holds events that are yet to be entered into the database.
     */
    protected Queue<RecordingEvent> eventQueue = new LinkedList<>();

    public Future<DatabaseHandler> makeNew(long startTime) throws IOException {
        File database = DATABASE_FOLDER.resolve(String.format("%d.sqlite", startTime)).toFile();
        int i = 1;
        while (!database.createNewFile()) {
            if (i > 20) {
                throw new IOException("Tried twenty different file names, I give up.");
            }
            database = DATABASE_FOLDER.resolve(String.format("%d_%d.sqlite", startTime, i)).toFile();
            i++;
        }
        File finalDatabase = database;
        FutureTask<DatabaseHandler> future = new FutureTask<>(() -> {
            DatabaseHandler handler = new DatabaseHandler(finalDatabase);
            handler.initDatabase(startTime);
            return handler;
        });
        new Thread(future, "Autocut Database Initialization Thread").start();
        return future;
    }

    /**
     * May be slow.
     */
    private void initDatabase(long startTime) throws SQLException {
        Autocut.LOGGER.debug("Initializing database");
        Autocut.LOGGER.debug("Acquiring lock");
        synchronized (databaseLock) {
            Autocut.LOGGER.debug("Lock acquired");
            try (Connection connection = DriverManager.getConnection(getDatabaseUrl())) {
                Autocut.LOGGER.debug("Creating events table");
                try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("""
                    CREATE TABLE %s (
                        %s INTEGER PRIMARY KEY AUTOINCREMENT,
                        %s INTEGER,
                        %s TEXT,
                        %s INTEGER
                    );""", "events", "id", "recording_trigger", "object", "time"));
                }
                try (Statement statement = connection.createStatement()) {
                    Autocut.LOGGER.debug("Creating recording_triggers table");
                    statement.execute(String.format("""
                        CREATE TABLE %s (
                            %s INTEGER UNIQUE ON CONFLICT FAIL,
                            %s TEXT
                        );""", "recording_triggers", "recording_trigger", "name"));
                }
                try (Statement statement = connection.createStatement()) {
                    Autocut.LOGGER.debug("Creating meta table");
                    statement.execute(String.format("""
                        CREATE TABLE %s (
                            %s TEXT UNIQUE ON CONFLICT FAIL,
                            %s TEXT
                        );""", "meta", "key", "value"));
            }
                        );""", META, "meta_key", "meta_value"));
                }
                try (PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?, ?);", META))) {
                    Autocut.LOGGER.debug("Inserting start_timestamp");
                    statement.setString(1, "start_timestamp");
                    statement.setLong(2, startTime);
                    statement.execute();
                }
            }
            Autocut.LOGGER.debug("Releasing lock");
        }
        Autocut.LOGGER.debug("Finished initializing database");
    }

    private String getDatabaseUrl() {
        return "jdbc:sqlite:" + database.getPath();
    }

    /**
     * Load from file.
     * @param database The file to load from. Must exist. Assumed to be a sqlite file. Assumed to be initialized.
     */
    public DatabaseHandler load(File database) {
        if (!database.exists()) throw new IllegalArgumentException("Database must exist.");
        return new DatabaseHandler(database);
    }

    /**
     * @param database The database file. Must exist. Assumed to be a sqlite file.
     */
    private DatabaseHandler(File database) {
        this.database = database;
    }
}
