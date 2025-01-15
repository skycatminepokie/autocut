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
    /**
     * The database file. Always exists and is a sqlite database.
     */
    private final File database;

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
            handler.initDatabase();
            return handler;
        });
        new Thread(future, "Autocut Database Initialization Thread").start();
        return future;
    }

    /**
     * May be slow.
     */
    private void initDatabase() throws SQLException {
        Autocut.LOGGER.debug("Initializing database");
        try (Connection connection = DriverManager.getConnection(getDatabaseUrl())) {
            Autocut.LOGGER.debug("Creating events table");
            try (Statement statement = connection.createStatement()) {
                    statement.execute(String.format("""
                    CREATE TABLE %s (
                        %s INTEGER PRIMARY KEY AUTOINCREMENT,
                        %s INTEGER,
                        %s TEXT,
                        %s INTEGER
                    );""", "events", "id", "trigger", "text", "time"));
            }
            try (Statement statement = connection.createStatement()) {
                Autocut.LOGGER.debug("Creating triggers table");
                statement.execute(String.format("""
                        CREATE TABLE %s (
                            %s INTEGER UNIQUE ON CONFLICT FAIL,
                            %s TEXT
                        );""", "triggers", "trigger", "name"));
            }
            try (Statement statement = connection.createStatement()) {
                Autocut.LOGGER.debug("Creating meta table");
                statement.execute(String.format("""
                        CREATE TABLE %s (
                            %s TEXT UNIQUE ON CONFLICT FAIL,
                            %s TEXT
                        );""", "meta", "key", "value"));
            }
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
