package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A clip created by shooting a player with an arrow.
 */
public class ShootPlayerClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "shoot_player");
    public static final Codec<ShootPlayerClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ShootPlayerClipType::new));

    public ShootPlayerClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset) {
        super(ID, active, shouldRecord, startOffset, endOffset, true, true, 100, 100);
    }

    public ShootPlayerClipType() {
        super(ID, true, true, 100, 100, true, true, 100, 100);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.shoot_player_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.shoot_player_clip");
    }

    public Clip createClip(long time, ClientPlayerEntity player) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Shot a player with an arrow", player.getNameForScoreboard(), null, player.getPos(), null);
    }
}
