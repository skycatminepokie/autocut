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
public record Clip(long in, long time, long out, @NotNull Identifier type, boolean active, boolean inverse, @Nullable String description,
                   @Nullable String source, @Nullable String object, @Nullable Vec3d sourceLocation,
                   @Nullable Vec3d objectLocation) {
    public Clip {
        assert in < out;
    }

    /**
     * @return A deep copy of this clip
     */
    public Clip copy() { // Deep copy, though since everything inside is immutable that doesn't mean much.
        return new Clip(in, time, out, type, active, inverse, description, source, object, sourceLocation, objectLocation);
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
     *
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
}
