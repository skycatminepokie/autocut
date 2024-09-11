package com.skycatdev.autocut.clips;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class ClipType {
    public abstract @NotNull Identifier getId();
    public abstract long getStartOffset();
    public abstract long getEndOffset();
    public abstract boolean enabled();
    protected ClipBuilder baseClip(long time) {
        return new ClipBuilder(time - getStartOffset(), time, time - getEndOffset(), getId());
    }
}
