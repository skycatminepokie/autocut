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
    public static final Identifier TYPE_REGISTRY_ID = Identifier.of(Autocut.MOD_ID, "clip_types");
    public static final Identifier CODEC_REGISTRY_ID = Identifier.of(Autocut.MOD_ID, "clip_type_codecs");
    public static final Registry<ClipType> TYPE_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(TYPE_REGISTRY_ID), Lifecycle.stable());
    public static final Registry<Codec<? extends ClipType>> CODEC_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(CODEC_REGISTRY_ID), Lifecycle.stable());
    public static final BreakBlockClipType BREAK_BLOCK = registerClipType(BreakBlockClipType.ID, BreakBlockClipType.CODEC, BreakBlockClipType::new);
    public static final AttackEntityClipType ATTACK_ENTITY = registerClipType(AttackEntityClipType.ID, AttackEntityClipType.CODEC, AttackEntityClipType::new);
    public static final DeathClipType DEATH = registerClipType(DeathClipType.ID, DeathClipType.CODEC, DeathClipType::new);
    public static final PlaceBlockClipType PLACE_BLOCK = registerClipType(PlaceBlockClipType.ID, PlaceBlockClipType.CODEC, PlaceBlockClipType::new);
    public static final ShootPlayerClipType SHOOT_PLAYER = registerClipType(ShootPlayerClipType.ID, ShootPlayerClipType.CODEC, ShootPlayerClipType::new);
    public static final TakeDamageClipType TAKE_DAMAGE = registerClipType(TakeDamageClipType.ID, TakeDamageClipType.CODEC, TakeDamageClipType::new);
    public static final UseItemClipType USE_ITEM = registerClipType(UseItemClipType.ID, UseItemClipType.CODEC, UseItemClipType::new);

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

    private static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec) {
        File configFile = configPathForId(typeId).toFile();
        return readClipType(typeId, typeCodec, configFile);
    }

    private static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec, File configFile) {
        T clipType;
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            var result = typeCodec.decode(JsonOps.INSTANCE, json);
            clipType = result.getOrThrow().getFirst();
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
    public static <T extends ClipType> T registerClipType(Identifier typeId, Codec<T> typeCodec, Supplier<T> defaultSupplier) {
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
        Registry.register(CODEC_REGISTRY, typeId, typeCodec);
        return Registry.register(TYPE_REGISTRY, typeId, clipType);
    }

    private static <T extends ClipType> void saveClipType(Codec<T> typeCodec, T clipType) {
        Identifier typeId = clipType.getId();
        File configFile = configPathForId(typeId).toFile();
        saveClipType(typeCodec, clipType, configFile);
    }

    private static <T extends ClipType> void saveClipType(Codec<T> typeCodec, T clipType, File configFile) {
        JsonElement serialized = typeCodec.encode(clipType, JsonOps.INSTANCE, JsonOps.INSTANCE.empty()).getOrThrow();
        try (PrintWriter writer = new PrintWriter(configFile); JsonWriter jsonWriter = new JsonWriter(writer)) {
            Streams.write(serialized, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save clip type " + clipType.getId() + ".");
        }
    }
}
