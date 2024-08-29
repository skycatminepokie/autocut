package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.ClipBuilder;
import com.skycatdev.autocut.clips.ClipTypes;
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
    @Nullable public static RecordingHandler currentRecordingHandler = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
        ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
            if (currentRecordingHandler != null) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingHandler.addClip(new ClipBuilder(time - 250, time, time + 250, ClipTypes.BREAK_BLOCK)
                            .setDescription("Broke " + state.getBlock().getName().getString())
                            .setObject(Registries.BLOCK.getId(state.getBlock()).toString())
                            .setObjectLocation(Vec3d.of(pos))
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .build()); // TODO: Localize
                } catch (SQLException ignored) { // TODO
                }
            }
        }));
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (currentRecordingHandler != null && entity != null) {
                long time = System.currentTimeMillis();
                try {
                    ClipBuilder builder = new ClipBuilder(time - 250, time, time + 250, ClipTypes.ATTACK_ENTITY)
                            .setDescription("Attacked " + entity.getName())
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .setObject(entity.getNameForScoreboard())
                            .setObjectLocation(entity.getPos());
                    currentRecordingHandler.addClip(builder.build());
                } catch (SQLException ignored) { // TODO
                }
            }
            return ActionResult.PASS;
        }));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (currentRecordingHandler != null) {
                long time = System.currentTimeMillis();
                try {
                    ItemStack itemStack = player.getStackInHand(hand);
                    ClipBuilder builder = new ClipBuilder(time - 250, time, time + 250, ClipTypes.USE_ITEM)
                            .setDescription("Used " + itemStack.getName().getString())
                            .setSource(player.getNameForScoreboard())
                            .setSourceLocation(player.getPos())
                            .setObject(Registries.ITEM.getId(itemStack.getItem()).toString());
                    currentRecordingHandler.addClip(builder.build());
                } catch (SQLException ignored) { // TODO
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
    }
}