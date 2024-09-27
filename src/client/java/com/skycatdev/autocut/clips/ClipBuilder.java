package com.skycatdev.autocut.clips;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClipBuilder {
    private long in;
    private long time;
    private long out;
    private @NotNull Identifier type;
    private boolean active = true;
    private boolean inverse;
    private @Nullable String description;
    private @Nullable String source;
    private @Nullable String object;
    private @Nullable Vec3d sourceLocation;
    private @Nullable Vec3d objectLocation;

    public ClipBuilder(long in, long time, long out, @NotNull Identifier type, boolean active, boolean inverse) {
        this.in = in;
        this.time = time;
        this.out = out;
        this.type = type;
        this.active = active;
        this.inverse = inverse;
    }

    public Clip build() {
        return new Clip(in, time, out, type, active, inverse, description, source, object, sourceLocation, objectLocation);
    }

    public ClipBuilder setActive(boolean active) {
        this.active = active;
        return this;
    }

    public ClipBuilder setDescription(@Nullable String description) {
        this.description = description;
        return this;
    }

    public ClipBuilder setIn(long in) {
        this.in = in;
        return this;
    }

    public ClipBuilder setInverse(boolean inverse) {
        this.inverse = inverse;
        return this;
    }

    public ClipBuilder setObject(@Nullable String object) {
        this.object = object;
        return this;
    }

    public ClipBuilder setObjectLocation(@Nullable Vec3d objectLocation) {
        this.objectLocation = objectLocation;
        return this;
    }

    public ClipBuilder setOut(long out) {
        this.out = out;
        return this;
    }

    public ClipBuilder setSource(@Nullable String source) {
        this.source = source;
        return this;
    }

    public ClipBuilder setSourceLocation(@Nullable Vec3d sourceLocation) {
        this.sourceLocation = sourceLocation;
        return this;
    }

    public ClipBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public ClipBuilder setType(@NotNull Identifier type) {
        this.type = type;
        return this;
    }
}