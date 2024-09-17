package com.skycatdev.autocut.clips;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

/**
 * A period of time in a recording. Timestamps are UNIX time, not relative to the recording.
 */
public class Clip {
    private final long in;
    private final long time;
    private final long out;
    private final @NotNull Identifier type;
    private final boolean active;
    private final @Nullable String description;
    private final @Nullable String source;
    private final @Nullable String object;
    private final @Nullable Vec3d sourceLocation;
    private final @Nullable Vec3d objectLocation;

    public Clip(long in, long time, long out, @NotNull Identifier type, boolean active, @Nullable String description, @Nullable String source, @Nullable String object, @Nullable Vec3d sourceLocation, @Nullable Vec3d objectLocation) {
        assert in < out;
        this.in = in;
        this.time = time;
        this.out = out;
        this.type = type;
        this.active = active;
        this.description = description;
        this.source = source;
        this.object = object;
        this.sourceLocation = sourceLocation;
        this.objectLocation = objectLocation;
    }

    /**
     * @return A deep copy of this clip
     */
    public Clip copy() { // Deep copy, though since everything inside is immutable that doesn't mean much.
        return new Clip(in, time, out, type, active, description, source, object, sourceLocation, objectLocation);
    }

    /**
     * @return the duration of the clip in milliseconds
     */
    public long duration() {
        return out - in;
    }

    /**
     * Converts the clip to a {@code between} statement for ffmpeg.
     *
     * @param variable           The variable for time in seconds.
     * @param recordingStartTime The time the relevant recording started, used for offsetting the clip time
     * @return a {@code between} statement for ffmpeg statements.
     */
    public String toBetweenStatement(String variable, long recordingStartTime) {
        double inSecs = (double) (in - recordingStartTime) / 1000;
        double outSecs = (double) (out - recordingStartTime) / 1000;
        return String.format("between(%s\\,%f\\,%f)", variable, inSecs, outSecs);
    }

    /**
     * Converts the clip to a range for FFmpeg's trim filter.
     * @param startTime The time the recording started at
     * @return A range for FFmpeg's trim filter
     */
    public String toTrimRange(long startTime) {
        return String.format("%dms:%dms", in - startTime, out - startTime);
    }

    public static long totalDuration(Collection<Clip> clips) {
        long duration = 0;
        for (Clip clip : clips) {
            duration += clip.duration();
        }
        return duration;
    }

    public long in() {
        return in;
    }

    public long time() {
        return time;
    }

    public long out() {
        return out;
    }

    public @NotNull Identifier type() {
        return type;
    }

    public @Nullable String description() {
        return description;
    }

    public @Nullable String source() {
        return source;
    }

    public @Nullable String object() {
        return object;
    }

    public @Nullable Vec3d sourceLocation() {
        return sourceLocation;
    }

    public @Nullable Vec3d objectLocation() {
        return objectLocation;
    }

    public boolean active() {
        return active;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Clip) obj;
        return this.in == that.in &&
               this.time == that.time &&
               this.out == that.out &&
               Objects.equals(this.type, that.type) &&
               Objects.equals(this.description, that.description) &&
               Objects.equals(this.source, that.source) &&
               Objects.equals(this.object, that.object) &&
               Objects.equals(this.sourceLocation, that.sourceLocation) &&
               Objects.equals(this.objectLocation, that.objectLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(in, time, out, type, description, source, object, sourceLocation, objectLocation);
    }

    @Override
    public String toString() {
        return "Clip[" +
               "in=" + in + ", " +
               "time=" + time + ", " +
               "out=" + out + ", " +
               "type=" + type + ", " +
               "description=" + description + ", " +
               "source=" + source + ", " +
               "object=" + object + ", " +
               "sourceLocation=" + sourceLocation + ", " +
               "objectLocation=" + objectLocation + ']';
    }
}
