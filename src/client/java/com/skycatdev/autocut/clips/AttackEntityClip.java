package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A clip created by attempting to attack an entity.
 */
@Configurable("attack_entity_clip")
public class AttackEntityClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "attack_entity");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;
    @Configurable("default_active")
    public static boolean defaultActive = true;
    public AttackEntityClip(long time, PlayerEntity player, Entity entity) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, defaultActive, "Attacked " + entity.getNameForScoreboard(), player.getNameForScoreboard(), entity.getType().getName().getString(), player.getPos(), entity.getPos());
    }
}
