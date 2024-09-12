package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.clips.ClipTypes;
import com.skycatdev.autocut.clips.UseItemClip;
import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public class AutocutClient implements ClientModInitializer {
    @Nullable public static OBSRemoteController controller = null;
    @Nullable public static RecordingManager currentRecordingManager = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
        ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
            if (currentRecordingManager != null) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(new ClipBuilder(time - 250, time, time + 250, ClipTypes.BREAK_BLOCK)
                            .setDescription("Broke " + state.getBlock().getName().getString()) // TODO: Localize
                            .setObject(Registries.BLOCK.getId(state.getBlock()).toString())
                            .setObjectLocation(Vec3d.of(pos))
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .build());
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store block break event", e);
                }
            }
        }));
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (currentRecordingManager != null && entity != null) {
                long time = System.currentTimeMillis();
                try {
                    ClipBuilder builder = new ClipBuilder(time - 250, time, time + 250, ClipTypes.ATTACK_ENTITY)
                            .setDescription("Attacked " + entity.getName())
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .setObject(entity.getNameForScoreboard())
                            .setObjectLocation(entity.getPos());
                    currentRecordingManager.addClip(builder.build());
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store entity attack event", e);
                }
            }
            return ActionResult.PASS;
        }));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (currentRecordingManager != null && UseItemClip.shouldRecord) {
                long time = System.currentTimeMillis();
                try {
                    ItemStack itemStack = player.getStackInHand(hand);
                    currentRecordingManager.addClip(new UseItemClip(time, player, itemStack));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store use item event", e);
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
    }
}