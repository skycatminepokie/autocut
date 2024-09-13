package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A clip created by the user breaking a block
 */
@Configurable("break_block_clip")
public class BreakBlockClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "break_block");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;
    public BreakBlockClip(long time, ClientPlayerEntity player, BlockPos pos, BlockState state) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, true, "Broke " + state.getBlock().getName().toString(), player.getNameForScoreboard(), Registries.BLOCK.getId(state.getBlock()).toString(), player.getPos(), Vec3d.of(pos));
    }
}
