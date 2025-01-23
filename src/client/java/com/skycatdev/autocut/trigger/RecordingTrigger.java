package com.skycatdev.autocut.trigger;

import com.mojang.serialization.Codec;

public abstract class RecordingTrigger {
	protected RecordingTriggerType<?> type;
	public static final Codec<RecordingTrigger> CODEC = RecordingTriggerTypes.REGISTRY.getCodec()
			.dispatch(RecordingTrigger::getType, RecordingTriggerType::codec);

	public RecordingTrigger(RecordingTriggerType<?> type) {
		this.type = type;
	}

	public RecordingTriggerType<?> getType() {
		return type;
	}
}
