package com.skycatdev.autocut.trigger;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.skycatdev.autocut.Autocut;
import net.minecraft.util.Identifier;

public class ManualTrigger extends RecordingTrigger {
	public ManualTrigger() {
		super(RecordingTriggerTypes.MANUAL);
	}

	public static class Type extends RecordingTriggerType<ManualTrigger> {
		public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "manual");
		public static final MapCodec<ManualTrigger> CODEC = MapCodec.unit(ManualTrigger::new);

		public Type() {
			super(ID, CODEC);
		}

		@Override
		public ManualTrigger makeDefault() {
			return new ManualTrigger();
		}

		public void trigger() {
			storeEvent(new JsonObject(), System.currentTimeMillis());
		}
	}
}
