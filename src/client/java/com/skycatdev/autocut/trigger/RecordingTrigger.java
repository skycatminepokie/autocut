package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.record.RecordingEvent;
import net.minecraft.util.Identifier;

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
			AutocutClient.currentDatabaseHandler.queueEvent(new RecordingEvent(id, object, time));
		}
	}
}
