package com.skycatdev.autocut;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Utils {
    /**
     * Reads something from JSON in a {@link File} with its {@link Codec}.
     * @param file The file to read from. Must exist, be readable, and not be a directory
     * @param codec The codec to use.
     * @return A new {@link T}
     * @throws IOException When something goes wrong.
     */
    public static <T> T readFromJson(File file, Codec<T> codec) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            DataResult<Pair<T, JsonElement>> result = codec.decode(JsonOps.INSTANCE, json);
            T t;
            //? if >=1.21 {
            t = result.getOrThrow(IOException::new).getFirst();
            //?} else {
            /*var optional = result.result();
            if (optional.isPresent()) {
                t = optional.get().getFirst();
            } else {
                Autocut.LOGGER.error("Autocut failed to load from JSON. Logging DFU message, then throwing error.");
                try {
                    result.getOrThrow(false, Autocut.LOGGER::error);
                } catch (RuntimeException e) {
                    throw new IOException("Autocut failed to read from JSON.", e);
                }
                throw new IllegalStateException("Bro, DFU was supposed to throw a RuntimeException. Ugh, well it failed, that's all the info I got for ya.");
            }
            *///?}
            return t;
        }
    }

    /**
     * Writes something to JSON in a {@link File} with its {@link Codec}.
     * @param file The file to save to. Must exist, be writable, and not be a directory.
     * @param codec The codec to use.
     * @param instance The thing to serialize.
     */
    public static <T> void saveToJson(File file, Codec<T> codec, T instance) throws IOException {
        DataResult<JsonElement> dataResult = codec.encode(instance, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        // 1.12.1
        //? if >=1.21 {
        JsonElement serialized = dataResult.getOrThrow(IOException::new);
        //?} else {
        /*var optional = dataResult.result();
        JsonElement serialized;
        if (optional.isPresent()) {
            serialized = optional.get();
        } else {
            Autocut.LOGGER.error("Autocut failed to save to JSON. Logging DFU message, then throwing error.");
            try {
                dataResult.getOrThrow(false, Autocut.LOGGER::error);
            } catch (RuntimeException e) {
                throw new IOException("Autocut failed to write to JSON.", e);
            }
            throw new IllegalStateException("Bro, DFU was supposed to throw a RuntimeException. Ugh, well it failed, that's all the info I got for ya.");
        }
        *///?}
        try (PrintWriter writer = new PrintWriter(file); JsonWriter jsonWriter = new JsonWriter(writer)) {
            Streams.write(serialized, jsonWriter);
        }
    }
}
