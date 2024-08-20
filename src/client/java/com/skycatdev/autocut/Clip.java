package com.skycatdev.autocut;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A period of time in a recording. Timestamps are UNIX time, not relative to the recording.
 *
 * @param in          The timestamp to begin, in milliseconds
 * @param out         The timestamp to end, in milliseconds
 * @param type        The type of clip this is
 * @param description A short, human-readable description of what the clip contains
 */
public record Clip(long in, long out, Identifier type, @Nullable String description) {
    public Clip {
        assert in < out;
    }

    /**
     * @return the duration of the clip in milliseconds
     */
    public long duration() {
        return out - in;
    }

    /**
     * Converts the clip to a {@code between} statement for ffmpeg.
     * @param variable The variable for time in seconds.
     * @param recordingStartTime The time the relevant recording started, used for offsetting the clip time
     * @return a {@code between} statement for ffmpeg statements.
     */
    public String toBetweenStatement(String variable, long recordingStartTime) {
        double inSecs = (double) (in - recordingStartTime) / 1000;
        double outSecs = (double) (out - recordingStartTime) / 1000;
        return String.format("between(%s\\,%f\\,%f)", variable, inSecs, outSecs);
    }

    /**
     * @return A deep copy of this clip
     */
    public Clip copy() { // Deep copy, though since everything inside is immutable that doesn't mean much.
        return new Clip(in, out, type, description);
    }
}
