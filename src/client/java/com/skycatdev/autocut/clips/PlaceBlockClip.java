package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * A clip created by successfully placing a block.
 */
@Configurable("place_block_clip")
public class PlaceBlockClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "place_block");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;
    @Configurable("default_active")
    public static boolean defaultActive = true;

    public PlaceBlockClip(long time, ClientPlayerEntity player, ItemStack itemStack, BlockPos placementLocation) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, defaultActive, "Placed " + itemStack.getName().getString(), player.getNameForScoreboard(), Registries.ITEM.getId(itemStack.getItem()).toString(), player.getPos(), Vec3d.of(placementLocation));
    }
}
