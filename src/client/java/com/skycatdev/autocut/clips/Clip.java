package com.skycatdev.autocut.clips;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeSet;
import com.skycatdev.autocut.database.ClipType;
import com.skycatdev.autocut.trigger.RecordingTrigger;

import java.util.Collection;

/**
 * A period of time in a record. Timestamps are UNIX time, not relative to the record.
 */
public record Clip(long in, long out, RecordingTrigger trigger, ClipType type) {
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
     *
     * @param variable           The variable for time in seconds.
     * @param recordingStartTime The time the relevant record started, used for offsetting the clip time
     * @return a {@code between} statement for ffmpeg statements.
     */
    public String toBetweenStatement(String variable, long recordingStartTime) {
        double inSecs = (double) (in - recordingStartTime) / 1000;
        double outSecs = (double) (out - recordingStartTime) / 1000;
        return String.format("between(%s\\,%f\\,%f)", variable, inSecs, outSecs);
    }

    public Range<Long> toRange() {
        return Range.closed(in, out);
    }

    public static TreeRangeSet<Long> toRange(Collection<Clip> clips) {
        TreeRangeSet<Long> range = TreeRangeSet.create();
        for (Clip clip : clips) {
            if (!clip.type().isInverted()) {
                range.add(clip.toRange());
            }
        }
        for (Clip clip : clips) {
            if (clip.type().isInverted()) {
                range.remove(clip.toRange());
            }
        }
        return range;
    }

    /**
     * Converts the clip to a range for FFmpeg's trim filter.
     *
     * @param startTime The time the record started at
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
