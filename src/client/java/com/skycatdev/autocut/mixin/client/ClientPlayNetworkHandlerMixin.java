package com.skycatdev.autocut.mixin.client;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.clips.ClipTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.sql.SQLException;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameStateChange", at = @At(value = "FIELD", target = "Lnet/minecraft/network/packet/s2c/play/GameStateChangeS2CPacket;PROJECTILE_HIT_PLAYER:Lnet/minecraft/network/packet/s2c/play/GameStateChangeS2CPacket$Reason;"))
    private void autocut$onArrowHitPlayer(GameStateChangeS2CPacket packet, CallbackInfo ci) {
        if (AutocutClient.currentRecordingManager != null) {
            long time = System.currentTimeMillis();
            try {
                ClientPlayerEntity player = MinecraftClient.getInstance().player;
                if (player != null) {
                    ClipBuilder builder = new ClipBuilder(time - 100, time, time + 100, ClipTypes.SHOOT_PLAYER)
                            .setDescription("Shot a player with an arrow")
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos());
                    AutocutClient.currentRecordingManager.addClip(builder.build());
                }
            } catch (SQLException e) {
                Autocut.LOGGER.warn("Unable to store player shot event", e);
            }
        }
    }
}
