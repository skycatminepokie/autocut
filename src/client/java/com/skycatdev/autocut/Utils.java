package com.skycatdev.autocut;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Utils {
    public static <T> T readFromJson(File file, Codec<T> codec) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            DataResult<Pair<T, JsonElement>> result = codec.decode(JsonOps.INSTANCE, json);
            T t;
            //? if >=1.21 {
            t = result.getOrThrow(IOException::new).getFirst();
            //?} else {
            /*t = result.getOrThrow(false, (message) -> {throw new IOException(message);}).getFirst();
             *///?}
            return t;
        }
    }
}
