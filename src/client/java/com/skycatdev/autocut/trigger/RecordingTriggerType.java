package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.record.RecordingEvent;
import net.minecraft.util.Identifier;

import java.util.concurrent.ExecutionException;

public abstract class RecordingTriggerType<T extends RecordingTrigger> {
	private final Identifier id;
	private final MapCodec<T> codec;

	public RecordingTriggerType(Identifier id, MapCodec<T> codec) {
		this.id = id;
		this.codec = codec;
	}

	public Identifier id() {
		return id;
	}

	public MapCodec<T> codec() {
		return codec;
	}

	public abstract T makeDefault();

	protected void storeEvent(JsonObject object, long time) {
		if (AutocutClient.currentDatabaseHandler != null) {
			try {
				AutocutClient.currentDatabaseHandler.get().queueEvent(new RecordingEvent(id(), object, time));
			} catch (InterruptedException | ExecutionException e) {
				// Shouldn't really happen
				throw new RuntimeException(e);
			}
		}
	}

}
