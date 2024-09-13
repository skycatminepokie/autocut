package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * A clip created by attempting to use an item.
 */
@Configurable("use_item_clip")
public class UseItemClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "use_item");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;
    @Configurable("default_active")
    public static boolean defaultActive = true;
    public UseItemClip(long time, PlayerEntity player, ItemStack itemStack) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, defaultActive, "Used " + itemStack.getName().getString(), player.getNameForScoreboard(), Registries.ITEM.getId(itemStack.getItem()).toString(), player.getPos(), null);
    }
}
