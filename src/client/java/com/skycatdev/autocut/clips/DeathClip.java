package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A clip created by successfully dying.
 */
@Configurable("death_clip")
public class DeathClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "death");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;

    public DeathClip(long time, ClientPlayerEntity player, Text deathMessage) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, deathMessage.getString(), null, player.getNameForScoreboard(), null, player.getPos());
    }
}
