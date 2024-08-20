package com.skycatdev.autocut;

import net.minecraft.util.Identifier;

public class RecordingEvent {
    public long time;
    public Identifier type;
    public String description;

    public RecordingEvent(long time, Identifier type, String description) {
        this.time = time;
        this.type = type;
        this.description = description;
    }
}
