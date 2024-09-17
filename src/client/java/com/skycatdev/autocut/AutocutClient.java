package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.AttackEntityClip;
import com.skycatdev.autocut.clips.BreakBlockClip;
import com.skycatdev.autocut.clips.UseItemClip;
import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.BREAK_BLOCK;

public class AutocutClient implements ClientModInitializer {
    public static final QueuedMessageHandler QUEUED_MESSAGE_HANDLER = new QueuedMessageHandler();
    @Nullable public static OBSRemoteController controller = null;
    @Nullable public static RecordingManager currentRecordingManager = null;

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
        ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
            if (currentRecordingManager != null && BreakBlockClip.shouldRecord) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(BREAK_BLOCK.createClip(time, player, pos, state));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store block break event", e);
                }
            }
        }));
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (currentRecordingManager != null && entity != null && AttackEntityClip.shouldRecord) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(new AttackEntityClip(time, player, entity));
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
        ClientTickEvents.START_CLIENT_TICK.register(QUEUED_MESSAGE_HANDLER);

    }

}