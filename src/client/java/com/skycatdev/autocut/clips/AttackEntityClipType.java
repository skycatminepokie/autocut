package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
/**
 * A clip created by attempting to attack an entity.
 */
public class AttackEntityClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "attack_entity");
    public static final Codec<AttackEntityClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, AttackEntityClipType::new));

    public AttackEntityClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, exportGroupingMode, true, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    public AttackEntityClipType() {
        super(ID, true, true, 100, 100, false, ExportGroupingMode.NONE, true, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.attack_entity_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.attack_entity_clip");
    }

    public Clip createClip(long time, PlayerEntity player, Entity entity) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), isInverse(),"Attacked " + entity.getNameForScoreboard(), player.getNameForScoreboard(), entity.getType().getName().getString(), player.getPos(), entity.getPos());
    }
}
