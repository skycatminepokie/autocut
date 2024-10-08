package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
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

    public ShootPlayerClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, exportGroupingMode, true, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    public ShootPlayerClipType() {
        this(true, true, 100, 100, false, ExportGroupingMode.NONE);
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
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), isInverse(), getExportGroupingMode(), "Shot a player with an arrow", player.getNameForScoreboard(), null, player.getPos(), null);
    }
}
