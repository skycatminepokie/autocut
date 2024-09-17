package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A clip created by attempting to use an item.
 */
public class UseItemClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "use_item");
    public static final Codec<UseItemClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, UseItemClipType::new));

    public UseItemClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset) {
        super(ID, active, shouldRecord, startOffset, endOffset, true, true, 100, 100);
    }

    public UseItemClipType() {
        super(ID, true, true, 100, 100, true, true, 100, 100);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.use_item_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.use_item_clip");
    }

    public Clip createClip(long time, PlayerEntity player, ItemStack itemStack) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Used " + itemStack.getName().getString(), player.getNameForScoreboard(), Registries.ITEM.getId(itemStack.getItem()).toString(), player.getPos(), null);
    }
}
