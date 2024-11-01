package com.skycatdev.autocut.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.clips.Clip;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.NOT_IN_WORLD;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    // TODO: Check if paused on tick
    @WrapMethod(method = "setWorld")
    private void autocut$onSetWorld(@Nullable ClientWorld world, Operation<Void> original) {
        /*if (AutocutClient.currentRecordingManager != null && NOT_IN_WORLD.clipType().shouldRecord()) {
            long time = System.currentTimeMillis();
            if (world == null) {
                NOT_IN_WORLD.clipType().leaveWorld(time);
            } else {
                try {
                    Clip clip = NOT_IN_WORLD.clipType().enterWorld(time);
                    if (clip != null) {
                        AutocutClient.currentRecordingManager.addClip(clip);
                    }
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store not in world clip", e);
                }
            }
        }*/
        original.call(world);
    }
    
}
