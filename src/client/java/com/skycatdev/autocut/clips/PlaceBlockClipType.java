package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A clip created by successfully placing a block.
 */
public class PlaceBlockClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "place_block");
    public static final Codec<PlaceBlockClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, PlaceBlockClipType::new));

    public PlaceBlockClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, true, true, 100, 100, false);
    }

    public PlaceBlockClipType() {
        super(ID, true, true, 100, 100, false, true, true, 100, 100, false);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.place_block_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.place_block_clip");
    }

    public Clip createClip(long time, ClientPlayerEntity player, ItemStack itemStack, BlockPos placementLocation) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(),  isInverse(), "Placed " + itemStack.getName().getString(), player.getNameForScoreboard(), Registries.ITEM.getId(itemStack.getItem()).toString(), player.getPos(), Vec3d.of(placementLocation));
    }
}
