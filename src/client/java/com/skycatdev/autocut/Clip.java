package com.skycatdev.autocut;

/**
 * A period of time in a recording
 * @param in The timestamp to begin, in milliseconds
 * @param out The timestamp to end, in milliseconds
 */
public record Clip(long in, long out) {
    public Clip {
        assert in < out;
    }

    public long duration() {
        return out - in;
    }

    public String toBetweenStatement(String variable) {
        double inSecs = (double) in / 1000;
        double outSecs = (double) out / 1000;
        return String.format("between(%s\\,%f\\,%f)", variable, inSecs, outSecs);
    }

    public Clip copy() {
        return new Clip(in, out);
    }
}
