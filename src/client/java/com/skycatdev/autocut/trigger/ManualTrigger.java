package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.skycatdev.autocut.Autocut;
import net.minecraft.util.Identifier;

public class ManualTrigger extends RecordingTrigger {
	public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "manual");
	public static final Codec<ManualTrigger> CODEC = Codec.unit(ManualTrigger::new);

	protected ManualTrigger() {
		super(ID);
	}

	@Override
	public Codec<? extends RecordingTrigger> getCodec() {
		return CODEC;
	}

	public void trigger() {
		storeEvent(new JsonObject(), System.currentTimeMillis());
	}
}
