package com.skycatdev.autocut.record;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.skycatdev.autocut.Autocut;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public abstract class RecordingTrigger {
	public static final RegistryKey<Registry<RecordingTrigger>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(Autocut.MOD_ID, "triggers"));
	public static final SimpleRegistry<RecordingTrigger> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());

	protected final Identifier id;

	protected RecordingTrigger(Identifier id) {
		this.id = id;
	}

	public abstract Codec<? extends RecordingTrigger> getCodec();

	public Identifier getId() {
		return id;
	}
}
