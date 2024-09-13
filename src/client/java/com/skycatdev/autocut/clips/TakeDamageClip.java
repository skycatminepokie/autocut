package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.bawnorton.configurable.ControllerType;
import com.bawnorton.configurable.Yacl;
import com.skycatdev.autocut.Autocut;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * A clip created by taking damage.
 */
@Configurable("take_damage_clip")
public class TakeDamageClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "death");
    @Configurable(value = "default_start_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0, max = Long.MAX_VALUE, yacl = @Yacl(controller = ControllerType.LONG_FIELD))
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;
    /**
     * How many decimal points of precision to record damage at.
     */
    @Configurable(value = "damage_precision", min = 0, max = Integer.MAX_VALUE, yacl = @Yacl(controller = ControllerType.INTEGER_FIELD))
    public static int precision = 1;

    public TakeDamageClip(long time, ClientPlayerEntity player, float damageTaken) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, true, String.format(String.format("Took %%.%df damage", precision), damageTaken), null, player.getNameForScoreboard(), null, player.getPos());
    }
}
