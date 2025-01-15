package com.skycatdev.autocut.mixin.client;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.DEATH;
import static com.skycatdev.autocut.clips.ClipTypes.SHOOT_PLAYER;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
    @SuppressWarnings("unused")
    @Contract("_,_,_->fail")
    private ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
        throw new IllegalStateException("Implemented in a mixin, do not call.");
    }

    @Inject(method = "onGameStateChange", at = @At(value = "FIELD", target = "Lnet/minecraft/network/packet/s2c/play/GameStateChangeS2CPacket;PROJECTILE_HIT_PLAYER:Lnet/minecraft/network/packet/s2c/play/GameStateChangeS2CPacket$Reason;"))
    private void autocut$onArrowHitPlayer(GameStateChangeS2CPacket packet, CallbackInfo ci) {
        if (AutocutClient.currentRecordingManager != null) {
            long time = System.currentTimeMillis();
            try {
                ClientPlayerEntity player = client.player;
                if (player != null && SHOOT_PLAYER.clipType().shouldRecord()) {
                    AutocutClient.currentRecordingManager.addClip(SHOOT_PLAYER.clipType().createClip(time, player));
                }
            } catch (SQLException e) {
                Autocut.LOGGER.warn("Unable to store player shot event", e);
            }
        }
    }

    @Inject(method = "onDeathMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/client/network/ClientPlayerEntity;", ordinal = 1))
    private void autocut$onClientDeath(DeathMessageS2CPacket packet, CallbackInfo ci) {
        if (AutocutClient.currentRecordingManager != null) {
            long time = System.currentTimeMillis();
            try {
                ClientPlayerEntity player = client.player;
                if (player != null && DEATH.clipType().shouldRecord()) {
                    //? if >=1.20.5
                    AutocutClient.currentRecordingManager.addClip(DEATH.clipType().createClip(time, player, packet.message()));
                    //? if <1.20.5
                    /*AutocutClient.currentRecordingManager.addClip(DEATH.clipType().createClip(time, player, packet.getMessage()));*/

                }
            } catch (SQLException e) {
                Autocut.LOGGER.warn("Unable to store player death event", e);
            }
        }
    }
}
