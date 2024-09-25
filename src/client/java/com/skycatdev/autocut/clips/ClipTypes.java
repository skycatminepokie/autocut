package com.skycatdev.autocut.clips;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ConfigHandler;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class ClipTypes {
    public static final Identifier CLIP_TYPE_REGISTRY_ID = Identifier.of(Autocut.MOD_ID, "clip_types");
    public static final Registry<ClipTypeEntry<?>> CLIP_TYPE_REGISTRY = new SimpleRegistry<>(RegistryKey.ofRegistry(CLIP_TYPE_REGISTRY_ID), Lifecycle.stable());
    public static final ClipTypeEntry<BreakBlockClipType> BREAK_BLOCK = registerClipType(BreakBlockClipType.ID, BreakBlockClipType.CODEC, BreakBlockClipType::new);
    public static final ClipTypeEntry<AttackEntityClipType> ATTACK_ENTITY = registerClipType(AttackEntityClipType.ID, AttackEntityClipType.CODEC, AttackEntityClipType::new);
    public static final ClipTypeEntry<DeathClipType> DEATH = registerClipType(DeathClipType.ID, DeathClipType.CODEC, DeathClipType::new);
    public static final ClipTypeEntry<PlaceBlockClipType> PLACE_BLOCK = registerClipType(PlaceBlockClipType.ID, PlaceBlockClipType.CODEC, PlaceBlockClipType::new);
    public static final ClipTypeEntry<ShootPlayerClipType> SHOOT_PLAYER = registerClipType(ShootPlayerClipType.ID, ShootPlayerClipType.CODEC, ShootPlayerClipType::new);
    public static final ClipTypeEntry<TakeDamageClipType> TAKE_DAMAGE = registerClipType(TakeDamageClipType.ID, TakeDamageClipType.CODEC, TakeDamageClipType::new);
    public static final ClipTypeEntry<UseItemClipType> USE_ITEM = registerClipType(UseItemClipType.ID, UseItemClipType.CODEC, UseItemClipType::new);
    public static final ClipTypeEntry<ManualClipType> MANUAL = registerClipType(ManualClipType.ID, ManualClipType.CODEC, ManualClipType::new);
    public static final ClipTypeEntry<ReceivePlayerMessageClipType> RECEIVE_PLAYER_MESSAGE = registerClipType(ReceivePlayerMessageClipType.ID, ReceivePlayerMessageClipType.CODEC, ReceivePlayerMessageClipType::new);

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
        clipType = ConfigHandler.readClipTypeOrDefault(typeId, typeCodec, defaultSupplier);
        return Registry.register(CLIP_TYPE_REGISTRY, typeId, new ClipTypeEntry<>(typeCodec, clipType));
    }

}
