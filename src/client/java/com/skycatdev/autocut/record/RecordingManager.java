package com.skycatdev.autocut.record;

import com.skycatdev.autocut.database.DatabaseHandler;

public class RecordingManager {
    protected final DatabaseHandler databaseHandler;

	public RecordingManager(DatabaseHandler databaseHandler) {
		this.databaseHandler = databaseHandler;
	}

    public DatabaseHandler getDatabaseHandler() {
        return databaseHandler;
    }
}
