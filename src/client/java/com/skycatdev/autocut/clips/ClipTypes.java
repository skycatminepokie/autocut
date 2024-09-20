package com.skycatdev.autocut.clips;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ClipTypes {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autocut");
    public static final Identifier CLIP_TYPE_REGISTRY_ID = Identifier.of(Autocut.MOD_ID, "clip_types");
    public static final Registry<ClipTypeEntry<?>> CLIP_TYPE_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(CLIP_TYPE_REGISTRY_ID), Lifecycle.stable());
    public static final ClipTypeEntry<BreakBlockClipType> BREAK_BLOCK = registerClipType(BreakBlockClipType.ID, BreakBlockClipType.CODEC, BreakBlockClipType::new);
    public static final ClipTypeEntry<AttackEntityClipType> ATTACK_ENTITY = registerClipType(AttackEntityClipType.ID, AttackEntityClipType.CODEC, AttackEntityClipType::new);
    public static final ClipTypeEntry<DeathClipType> DEATH = registerClipType(DeathClipType.ID, DeathClipType.CODEC, DeathClipType::new);
    public static final ClipTypeEntry<PlaceBlockClipType> PLACE_BLOCK = registerClipType(PlaceBlockClipType.ID, PlaceBlockClipType.CODEC, PlaceBlockClipType::new);
    public static final ClipTypeEntry<ShootPlayerClipType> SHOOT_PLAYER = registerClipType(ShootPlayerClipType.ID, ShootPlayerClipType.CODEC, ShootPlayerClipType::new);
    public static final ClipTypeEntry<TakeDamageClipType> TAKE_DAMAGE = registerClipType(TakeDamageClipType.ID, TakeDamageClipType.CODEC, TakeDamageClipType::new);
    public static final ClipTypeEntry<UseItemClipType> USE_ITEM = registerClipType(UseItemClipType.ID, UseItemClipType.CODEC, UseItemClipType::new);

    /**
     * Adds the default fields to a {@code Codec<ClipType>}. Magic I cooked up with the help of Linguardium. Usage:
     * <pre>
     *     {@code
     *     // No extra fields
     *     public static final Codec<MyClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, MyClipType::new))
     *     // Extra fields
     *     public static final Codec<MyClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance)
     *             .and(Codec.BOOL.fieldOf("my_bool").forGetter(MyClipType::getMyBool))
     *             .apply(instance, MyClipType::new));
     *     }
     * </pre>
     *
     * @param <T> The inheritor of ClipType the codec is for.
     * @return The thing that you {@code .and} or {@code .apply} on.
     * @see net.minecraft.loot.entry.LeafEntry#addLeafFields(RecordCodecBuilder.Instance)
     */
    public static <T extends ClipType> Products.P4<RecordCodecBuilder.Mu<T>, Boolean, Boolean, Long, Long> addDefaultConfigFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.BOOL.fieldOf("should_record").forGetter(ClipType::shouldRecord),
                Codec.BOOL.fieldOf("active").forGetter(ClipType::isActive),
                Codec.LONG.fieldOf("start_offset").forGetter(ClipType::getStartOffset),
                Codec.LONG.fieldOf("end_offset").forGetter(ClipType::getEndOffset));
    }

    private static @NotNull Path configPathForId(Identifier typeId) {
        return CONFIG_PATH.resolve(typeId.getNamespace()).resolve(typeId.getPath() + ".json");
    }

    @SuppressWarnings("unused") // I want it around just in case
    private static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec) {
        File configFile = configPathForId(typeId).toFile();
        return readClipType(typeId, typeCodec, configFile);
    }

    private static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec, File configFile) {
        T clipType;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            var result = typeCodec.decode(JsonOps.INSTANCE, json);
            // 1.12.1
            //? if >=1.21.1 {
            clipType = result.getOrThrow().getFirst();
            //?} else {
            /*clipType = result.getOrThrow(false, (a)-> {throw new RuntimeException(a);}).getFirst();
            *///?}

        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize clipType of id " + typeId + ". For a quick fix, try deleting the config file " + configFile.getAbsolutePath() + ". You may lose configs.");
        }
        return clipType;
    }

    /**
     * Registers a {@link ClipType} and its {@link Codec} for use in Autocut.
     *
     * @param typeId          The ClipType's ID
     * @param typeCodec       A codec for serializing/deserializing the ClipType itself - probably only config
     * @param defaultSupplier A {@link Supplier} to get a default {@link T}.
     * @return {@code typeCodec}
     */
    public static <T extends ClipType> ClipTypeEntry<T> registerClipType(Identifier typeId, Codec<T> typeCodec, Supplier<T> defaultSupplier) {
        T clipType;
        Path configPath = configPathForId(typeId);
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            clipType = defaultSupplier.get();
            //noinspection ResultOfMethodCallIgnored
            configPath.getParent().toFile().mkdirs();
            try {
                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saveClipType(typeCodec, clipType, configFile);
        } else {
            clipType = readClipType(typeId, typeCodec, configFile);
        }
        return Registry.register(CLIP_TYPE_REGISTRY, typeId, new ClipTypeEntry<>(typeCodec, clipType));
    }

    private static <T extends ClipType> void saveClipType(ClipTypeEntry<T> clipTypeEntry) {
        Identifier typeId = clipTypeEntry.clipType().getId();
        File configFile = configPathForId(typeId).toFile();
        saveClipType(clipTypeEntry.codec(), clipTypeEntry.clipType(), configFile);
    }

    public static void saveAllClipTypes() {
        CLIP_TYPE_REGISTRY.forEach(ClipTypes::saveClipType);
    }

    private static <T extends ClipType> void saveClipType(Codec<T> typeCodec, T clipType, File configFile) {
        var dataResult = typeCodec.encode(clipType, JsonOps.INSTANCE, JsonOps.INSTANCE.empty());
        // 1.12.1
        //? if >=1.21.1 {
        JsonElement serialized = dataResult.getOrThrow();
         //?} else {
        /*JsonElement serialized = dataResult.getOrThrow(false, (a)-> {throw new RuntimeException(a);});
        *///?}
        try (PrintWriter writer = new PrintWriter(configFile); JsonWriter jsonWriter = new JsonWriter(writer)) {
            Streams.write(serialized, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save clip type " + clipType.getId() + ".");
        }
    }
}
