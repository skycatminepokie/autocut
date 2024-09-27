package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
/**
 * A clip created by successfully dying.
 */
public class DeathClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "death");
    public static final Codec<DeathClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, DeathClipType::new));

    public DeathClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, true, true, 100, 100, false);
    }

    public DeathClipType() {
        super(ID, true, true, 100, 100, false, true, true, 100, 100, false);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.death_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.death_clip");
    }

    public Clip createClip(long time, ClientPlayerEntity player, Text deathMessage) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), isInverse(), deathMessage.getString(), null, player.getNameForScoreboard(), null, player.getPos());
    }
}
