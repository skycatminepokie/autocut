package com.skycatdev.autocut.mixin.client;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.PLACE_BLOCK;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
    private void autocut$onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().equals(ActionResult.SUCCESS)) {
            if (AutocutClient.currentRecordingManager != null && context.getPlayer() instanceof ClientPlayerEntity player && PLACE_BLOCK.clipType().shouldRecord()) {
                try {
                    long time = System.currentTimeMillis();
                    AutocutClient.currentRecordingManager.addClip(PLACE_BLOCK.clipType().createClip(time, player, context.getStack(), context.getBlockPos()));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store block place event", e);
                }
            }
        }
    }
}
