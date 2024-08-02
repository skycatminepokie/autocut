package com.skycatdev.autocut;

public record Clip(long in, long out) {
    public long duration() {
        return out - in;
    }
}
