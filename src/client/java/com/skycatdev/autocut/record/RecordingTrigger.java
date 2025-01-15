package com.skycatdev.autocut.record;

import com.mojang.serialization.Codec;
import net.minecraft.util.Identifier;

// TODO
public class RecordingTrigger {
    public static final Codec<RecordingTrigger> CODEC = null; // TODO
    protected Identifier id;

    public Identifier getId() {
        return id;
    }
}
