package com.skycatdev.autocut;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

public class Utils {
    /**
     * Reads something from JSON in a {@link File} with its {@link Codec}.
     * @param file The file to read from. Must exist, be readable, and not be a directory
     * @param codec The codec to use.
     * @return A new {@link T}
     * @throws IOException When something goes wrong.
     */
    public static <T> @Nullable T readFromJson(File file, Codec<T> codec) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            JsonElement jsonElement = JsonParser.parseReader(reader);
            if (jsonElement == null || jsonElement.isJsonNull()) return null;
            JsonObject json = jsonElement.getAsJsonObject();
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

    public static String rangeToFFmpegRange(Range<Long> range, long startTime) {
        long rangeStart = range.lowerBoundType().equals(BoundType.OPEN) ? range.lowerEndpoint() + 1 : range.lowerEndpoint();
        long rangeEnd = range.upperBoundType().equals(BoundType.OPEN) ? range.upperEndpoint() - 1 : range.upperEndpoint();
        return String.format("%dms:%dms", rangeStart - startTime, rangeEnd - startTime);
    }

    /**
     * Calculates the total space covered by a set of non-overlapping ranges
     * @param ranges A collection of non-overlapping ranges
     * @return The total space covered by a set of non-overlapping ranges
     */
    public static long totalSpace(Collection<Range<Long>> ranges) {
        long totalSpace = 0;
        for (Range<Long> range : ranges) {
            long rangeStart = range.lowerBoundType().equals(BoundType.OPEN) ? range.lowerEndpoint() + 1 : range.lowerEndpoint();
            long rangeEnd = range.upperBoundType().equals(BoundType.OPEN) ? range.upperEndpoint() - 1 : range.upperEndpoint();
            totalSpace += rangeEnd - rangeStart;
        }
        return totalSpace;
    }
}
