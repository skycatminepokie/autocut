package com.skycatdev.autocut.clips;

import com.mojang.serialization.Lifecycle;
import com.skycatdev.autocut.Autocut;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

public class ClipTypes {
    public static final Identifier REGISTRY_ID = Identifier.of(Autocut.MOD_ID, "clip_types");
    public static final Registry<ClipType> REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(REGISTRY_ID), Lifecycle.stable());
    public static BreakBlockClipType BREAK_BLOCK = register(new BreakBlockClipType());

    public static <T extends ClipType> T register(T clipType) {
        return Registry.register(REGISTRY, clipType.getId(), clipType);
    }
}
