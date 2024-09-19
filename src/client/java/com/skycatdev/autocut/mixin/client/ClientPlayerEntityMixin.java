package com.skycatdev.autocut.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.TAKE_DAMAGE;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
    @WrapOperation(method = "updateHealth", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayerEntity;lastDamageTaken:F"))
    private void autocut$onUpdateHealth(ClientPlayerEntity player, float healthDifference, Operation<Void> original) {
        assert healthDifference > 0f;
        if (AutocutClient.currentRecordingManager != null) {
            long time = System.currentTimeMillis();
            try {
                if (player != null && TAKE_DAMAGE.clipType().shouldRecord()) {
                    AutocutClient.currentRecordingManager.addClip(TAKE_DAMAGE.clipType().createClip(time, player, healthDifference));
                }
            } catch (SQLException e) {
                Autocut.LOGGER.warn("Unable to store take damage event", e);
            }
        }
        original.call(player, healthDifference);
    }
}
