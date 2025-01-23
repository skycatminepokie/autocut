package com.skycatdev.autocut.trigger;

import com.mojang.serialization.Lifecycle;
import com.skycatdev.autocut.Autocut;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public class RecordingTriggerTypes {
	public static final RegistryKey<Registry<RecordingTriggerType<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Identifier.of(Autocut.MOD_ID, "triggers"));
	public static final SimpleRegistry<RecordingTriggerType<?>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());
	public static final AttackEntityTrigger.Type ATTACK_ENTITY = register(new AttackEntityTrigger.Type());
	public static final ManualTrigger.Type MANUAL = register(new ManualTrigger.Type());

	public static <T extends RecordingTriggerType<?>> T register(T triggerType) {
		return Registry.register(REGISTRY, triggerType.id(), triggerType);
	}

	public static void init() {}
}
