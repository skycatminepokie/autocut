package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.record.RecordingEvent;
import net.minecraft.util.Identifier;

import java.util.concurrent.ExecutionException;

public abstract class RecordingTrigger {
	protected final Identifier id;

	protected RecordingTrigger(Identifier id) {
		this.id = id;
	}

	public abstract Codec<? extends RecordingTrigger> getCodec();

	public Identifier getId() {
		return id;
	}

	protected void storeEvent(JsonObject object, long time) {
		if (AutocutClient.currentDatabaseHandler != null) {
			try {
				AutocutClient.currentDatabaseHandler.get().queueEvent(new RecordingEvent(id, object, time));
			} catch (InterruptedException | ExecutionException e) {
				// Shouldn't really happen
				throw new RuntimeException(e);
			}
		}
	}
}
