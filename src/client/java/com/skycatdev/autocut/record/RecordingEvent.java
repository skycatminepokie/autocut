package com.skycatdev.autocut.record;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;

public record RecordingEvent(Identifier trigger, JsonObject object, long time) {
}
