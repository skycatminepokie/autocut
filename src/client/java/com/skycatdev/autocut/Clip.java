package com.skycatdev.autocut;

public record Clip(Timecode in, Timecode out) {
    public Timecode duration() {
        return out.subtract(in);
    }

    public Clip {
        assert in.fps() == out().fps(); // TODO: Error handling
    }
}
