package com.skycatdev.autocut.trigger;

import com.mojang.serialization.Lifecycle;
import com.skycatdev.autocut.Autocut;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public class RecordingTriggers {
	public static final RegistryKey<Registry<RecordingTrigger>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(Autocut.MOD_ID, "triggers"));
	public static final SimpleRegistry<RecordingTrigger> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());
	public static final ManualTrigger MANUAL_TRIGGER = register(new ManualTrigger());

	public static <T extends RecordingTrigger> T register(T trigger) {
		return Registry.register(REGISTRY, trigger.getId(), trigger);
	}
}
