package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.ClipTypes;
import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.skycatdev.autocut.clips.ClipTypes.*;

public class AutocutClient implements ClientModInitializer {
    public static final KeyBinding CLIP_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.autocut.clip", InputUtil.UNKNOWN_KEY.getCode(), "key.category.autocut.autocut"));
    @Nullable public static OBSRemoteController controller = null;
    @Nullable public static RecordingManager currentRecordingManager = null;

    /**
     * Utility for consistently sending messages to the player from another thread.
     */
    public static void sendMessageOnClientThread(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.inGameHud.getChatHud().addMessage(text));
    }

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
        ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
            if (currentRecordingManager != null && BREAK_BLOCK.clipType().shouldRecord()) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(BREAK_BLOCK.clipType().createClip(time, player, pos, state));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store block break event", e);
                }
            }
        }));
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (currentRecordingManager != null && entity != null && ClipTypes.ATTACK_ENTITY.clipType().shouldRecord()) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(ATTACK_ENTITY.clipType().createClip(time, player, entity));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store entity attack event", e);
                }
            }
            return ActionResult.PASS;
        }));
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (currentRecordingManager != null && USE_ITEM.clipType().shouldRecord()) {
                long time = System.currentTimeMillis();
                try {
                    ItemStack itemStack = player.getStackInHand(hand);
                    currentRecordingManager.addClip(USE_ITEM.clipType().createClip(time, player, itemStack));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store use item event", e);
                }
            }
            return TypedActionResult.pass(ItemStack.EMPTY);
        });
        ClientTickEvents.END_CLIENT_TICK.register((client) -> { // TODO: Combine clips when held down, even if exporting separately
            if (CLIP_KEYBIND.wasPressed()) {
                if (currentRecordingManager != null && MANUAL.clipType().shouldRecord()) {
                    long time = System.currentTimeMillis();
                    try {
                        currentRecordingManager.addClip(MANUAL.clipType().createClip(time));
                    } catch (SQLException e) {
                        Autocut.LOGGER.warn("Unable to store manual clip", e);
                    }
                }
            }
        });
        ClientReceiveMessageEvents.CHAT.register(((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (currentRecordingManager != null && RECEIVE_PLAYER_MESSAGE.clipType().shouldRecord()) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingManager.addClip(RECEIVE_PLAYER_MESSAGE.clipType().createClip(time, message, sender));
                } catch (SQLException e) {
                    Autocut.LOGGER.warn("Unable to store player chat message clip", e);
                }
            }
        }));
    }

}