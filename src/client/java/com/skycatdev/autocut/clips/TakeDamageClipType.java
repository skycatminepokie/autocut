package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A clip created by taking damage.
 */
public class TakeDamageClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "take_damage");
    public static final Codec<TakeDamageClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance)
            .and(Codec.INT.fieldOf("precision").forGetter(TakeDamageClipType::getPrecision))
            .apply(instance, TakeDamageClipType::new));
    /**
     * How many decimal points of precision to record damage at.
     */
    private int precision;

    public TakeDamageClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, int precision) {
        super(ID, active, shouldRecord, startOffset, endOffset, true, true, 100, 100);
        this.setPrecision(precision);
    }

    public TakeDamageClipType() {
        super(ID, true, true, 100, 100, true, true, 100, 100);
        this.setPrecision(1);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.take_damage_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.take_damage_clip");
    }

    public Clip createClip(long time, ClientPlayerEntity player, float damageTaken) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), String.format(String.format("Took %%.%df damage", getPrecision()), damageTaken), null, player.getNameForScoreboard(), null, player.getPos());
    }

    public int getPrecision() {
        return precision;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }
}
