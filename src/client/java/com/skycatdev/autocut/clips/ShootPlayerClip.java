package com.skycatdev.autocut.clips;

import com.bawnorton.configurable.Configurable;
import com.skycatdev.autocut.Autocut;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Identifier;
/**
 * A clip created by shooting a player with an arrow.
 */
@Configurable("shoot_player_clip")
public class ShootPlayerClip extends Clip {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "shoot_player");
    @Configurable(value = "default_start_offset", min = 0)
    public static long defaultStartOffset = 100;
    @Configurable(value = "default_end_offset", min = 0)
    public static long defaultEndOffset = 100;
    @Configurable("should_record")
    public static boolean shouldRecord = true;

    public ShootPlayerClip(long time, ClientPlayerEntity player) {
        super(time - defaultStartOffset, time, time + defaultEndOffset, ID, "Shot a player with an arrow", player.getNameForScoreboard(), null, player.getPos(), null);
    }
}
