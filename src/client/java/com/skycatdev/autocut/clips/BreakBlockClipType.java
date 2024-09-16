package com.skycatdev.autocut.clips;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.datagen.AutocutEnglishLangProvider;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BreakBlockClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "break_block");
    public BreakBlockClipType() {
        super(ID, true, true, 100, 100, null);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + "break_block_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + "break_block_clip");
    }

    public Clip createClip(long time, ClientPlayerEntity player, BlockPos pos, BlockState state) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Broke " + state.getBlock().getName().getString(), player.getNameForScoreboard(), Registries.BLOCK.getId(state.getBlock()).toString(), player.getPos(), Vec3d.of(pos));
    }
}
