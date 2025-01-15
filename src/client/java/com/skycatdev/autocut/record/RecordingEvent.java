package com.skycatdev.autocut.record;

import com.google.gson.JsonObject;

public record RecordingEvent(RecordingTrigger trigger, JsonObject object, long time) {
}
