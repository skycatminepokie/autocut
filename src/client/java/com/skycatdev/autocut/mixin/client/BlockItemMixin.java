package com.skycatdev.autocut.mixin.client;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.clips.ClipTypes;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.sql.SQLException;

@Mixin(BlockItem.class)
public class BlockItemMixin {
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
    private void autocut$onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (cir.getReturnValue().equals(ActionResult.SUCCESS)) {
            if (AutocutClient.currentRecordingHandler != null && context.getPlayer() instanceof ClientPlayerEntity player) {
                long time = System.currentTimeMillis();
                try {
                    ItemStack itemStack = context.getStack();
                    ClipBuilder builder = new ClipBuilder(time - 250, time, time + 250, ClipTypes.PLACE_BLOCK)
                            .setDescription("Placed " + itemStack.getName().getString())
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .setObject(Registries.ITEM.getId(itemStack.getItem()).toString())
                            .setObjectLocation(Vec3d.of(context.getBlockPos()));
                    AutocutClient.currentRecordingHandler.addClip(builder.build());
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store block place event", e);
                }
            }
        }
    }
}
