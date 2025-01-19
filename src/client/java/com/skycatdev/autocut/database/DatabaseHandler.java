package com.skycatdev.autocut.database;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.export.Clip;
import com.skycatdev.autocut.record.RecordingEvent;
import net.fabricmc.loader.api.FabricLoader;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.FutureTask;

public class DatabaseHandler {
	/**
	 * The folder where databases are kept in by default.
	 */
	public static final Path DATABASE_FOLDER = FabricLoader.getInstance().getGameDir().resolve("autocutRecordings");
	public static final String META = "meta";
	public static final String EVENTS = "events";
	public static final String START_TIMESTAMP = "start_timestamp";
	public static final String META_KEY = "meta_key";
	public static final String META_VALUE = "value";
	public static final String RECORDING_PATH_KEY = "recording_path";
	public static final String RECORDING_TRIGGER = "recording_trigger";
	public static final String TIME = "time";
	public static final String OBJECT = "object";
	public static final String ID = "id";

	static {
		//noinspection ResultOfMethodCallIgnored
		DATABASE_FOLDER.toFile().mkdirs();
	}

	/**
	 * The database file. Always exists and is a sqlite database.
	 * Do not access the database before first acquiring {@link DatabaseHandler#databaseLock}.
	 */
	private final File database;
	private final Object[] databaseLock = new Object[0];
	private final Object[] queueStatusLock = new Object[0];
	/**
	 * Holds events that are yet to be entered into the database.
	 */
	private final ConcurrentLinkedQueue<RecordingEvent> eventQueue = new ConcurrentLinkedQueue<>();
	/**
	 * Whether the queue is being worked through. Do not access or modify without first acquiring {@link DatabaseHandler#queueStatusLock}
	 */
	private boolean queueRunning = false;

	/**
	 * @param database The database file. Must exist. Assumed to be a sqlite file.
	 */
	private DatabaseHandler(File database) {
		this.database = database;
	}

	/**
	 * Blocking, but likely fast
	 */
	private void ensureQueueRunning() {
		synchronized (queueStatusLock) {
			if (!queueRunning) {
				new Thread(this::writeQueue, "Autocut Event Queue");
			}
		}
	}

	public FutureTask<LinkedList<Clip>> generateClips(ArrayList<ClipType> clipTypes) {
		FutureTask<LinkedList<Clip>> task = new FutureTask<>(() -> {
			LinkedList<Clip> clips = new LinkedList<>();
			// for each clip type
			for (ClipType type : clipTypes) {
				if (type.endTrigger != null) {
					ResultSet triggers;
					synchronized (databaseLock) {
						try (Connection connection = DriverManager.getConnection(getDatabaseUrl());
							 PreparedStatement statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? OR %s = ? ORDER BY %s;", EVENTS, RECORDING_TRIGGER, RECORDING_TRIGGER, TIME))) {
							statement.setString(1, type.startTrigger.getId().toString());
							statement.setString(2, type.endTrigger.getId().toString());
							triggers = statement.executeQuery();
						}
					}
					LinkedList<Boolean> isStarts = new LinkedList<>();
					LinkedList<Long> times = new LinkedList<>();
					while (triggers.next()) {
						isStarts.add(triggers.getString(RECORDING_TRIGGER).equals(type.startTrigger.toString()));
						times.add(triggers.getLong(TIME));
					}
					assert isStarts.size() == times.size();

					Iterator<Boolean> isStartsIt = isStarts.iterator();
					Iterator<Long> timesIt = times.iterator();
					boolean lastIsStart = false;
					long lastTime = -1L;
					while (isStartsIt.hasNext()) {
						boolean isStart = isStartsIt.next();
						long time = timesIt.next();
						if (lastIsStart && !isStart) {
							clips.add(new Clip(lastTime - type.startOffset, time + type.endOffset, type));
						}
						lastIsStart = isStart;
						lastTime = time;
					}
				} else {
					synchronized (databaseLock) {
						ResultSet triggers;
						try (Connection connection = DriverManager.getConnection(getDatabaseUrl());
							 PreparedStatement statement = connection.prepareStatement(String.format("SELECT * FROM %s WHERE %s = ? ORDER BY %s;", EVENTS, RECORDING_TRIGGER, TIME))) {
							statement.setString(1, type.startTrigger.getId().toString());
							triggers = statement.executeQuery();
						}
						while (triggers.next()) {
							long time = triggers.getLong(TIME);
							clips.add(new Clip(time - type.startOffset, time + type.endOffset, type));
						}
					}
				}
			}
			return clips;
		});
		new Thread(task, "Autocut Clip Generator Thread").start();
		return task;
	}

	private String getDatabaseUrl() {
		return "jdbc:sqlite:" + database.getPath();
	}

	public FutureTask<String> getRecordingPath() {
		FutureTask<String> task = new FutureTask<>(() -> {
			synchronized (databaseLock) {
				String ret;
				try (Connection connection = DriverManager.getConnection(getDatabaseUrl()); Statement statement = connection.createStatement()) {
					ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = %s;", META_VALUE, META, META_KEY, RECORDING_PATH_KEY));
					rs.next();
					ret = rs.getString(1);
				}
				return ret;
			}
		});
		new Thread(task, "Autocut Recording Path Grabber Thread").start();
		return task;
	}

	public FutureTask<Long> getStartTime() {
		FutureTask<Long> task = new FutureTask<>(() -> {
			synchronized (databaseLock) {
				long ret;
				try (Connection connection = DriverManager.getConnection(getDatabaseUrl()); Statement statement = connection.createStatement()) {
					ResultSet rs = statement.executeQuery(String.format("SELECT %s FROM %s WHERE %s = %s;", META_VALUE, META, META_KEY, START_TIMESTAMP));
					rs.next();
					ret = rs.getLong(1);
				}
				return ret;
			}
		});
		new Thread(task, "Autocut Recording Time Grabber Thread").start();
		return task;
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
							);""", EVENTS, ID, RECORDING_TRIGGER, OBJECT, TIME));
				}
				try (Statement statement = connection.createStatement()) {
					Autocut.LOGGER.debug("Creating recording_triggers table");
					statement.execute(String.format("""
							CREATE TABLE %s (
							    %s TEXT UNIQUE ON CONFLICT FAIL,
							    %s TEXT
							);""", META, META_KEY, META_VALUE));
				}
				try (PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?, ?);", META))) {
					Autocut.LOGGER.debug("Inserting start_timestamp");
					statement.setString(1, START_TIMESTAMP);
					statement.setLong(2, startTime);
					statement.execute();
				}
			}
			Autocut.LOGGER.debug("Releasing lock");
		}
		Autocut.LOGGER.debug("Finished initializing database");
	}

	/**
	 * Load from file.
	 *
	 * @param database The file to load from. Must exist. Assumed to be a sqlite file. Assumed to be initialized.
	 */
	public DatabaseHandler load(File database) {
		if (!database.exists()) throw new IllegalArgumentException("Database must exist.");
		return new DatabaseHandler(database);
	}

	public static FutureTask<DatabaseHandler> makeNew(long startTime) throws IOException {
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

	public FutureTask<@Nullable Void> onRecordingEnded(String outputPath) {
		FutureTask<@Nullable Void> task = new FutureTask<>(() -> {
			synchronized (databaseLock) {
				try (Connection connection = DriverManager.getConnection(getDatabaseUrl());
					 PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s (%s, %s) VALUES (%s, ?);", META, META_KEY, META_VALUE, RECORDING_PATH_KEY))) {
					statement.setString(1, outputPath);
					statement.executeUpdate();
				}
			}
			return null;
		});
		new Thread(task, "Autocut Recording End Thread").start();
		return task;
	}

	public void queueEvent(RecordingEvent event) {
		eventQueue.add(event);
		new Thread(this::ensureQueueRunning, "Autocut Database Queue Runner").start();
	}

	/**
	 * Blocking, may take a long time. Synchronizes on the database.
	 */
	private void writeQueue() {
		if (eventQueue.isEmpty()) return;
		synchronized (databaseLock) {
			synchronized (queueStatusLock) {
				queueRunning = true;
			}
			try (Connection connection = DriverManager.getConnection(getDatabaseUrl());
				 PreparedStatement statement = connection.prepareStatement(String.format("INSERT INTO %s VALUES (?, ?, ?);", EVENTS))) {
				while (!eventQueue.isEmpty()) {
					RecordingEvent event = eventQueue.poll();
					statement.setString(1, event.trigger().toString());
					statement.setString(2, event.object().toString());
					statement.setLong(3, event.time());
					statement.execute();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
		synchronized (queueStatusLock) {
			queueRunning = false;
		}
	}
}
