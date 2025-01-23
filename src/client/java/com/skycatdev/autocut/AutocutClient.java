package com.skycatdev.autocut;

import com.skycatdev.autocut.config.Config;
import com.skycatdev.autocut.database.DatabaseHandler;
import com.skycatdev.autocut.trigger.RecordingTriggerTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.FutureTask;


public class AutocutClient implements ClientModInitializer {
    public static final KeyBinding CLIP_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.autocut.clip", InputUtil.UNKNOWN_KEY.getCode(), "key.category.autocut.autocut"));
    public static @Nullable FutureTask<DatabaseHandler> currentDatabaseHandler = null;
    public static Config config = Config.loadOrDefault();

    /**
     * Utility for consistently sending messages to the player from another thread.
     */
    public static void sendMessageOnClientThread(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.inGameHud.getChatHud().addMessage(text));
    }

    @Override
    public void onInitializeClient() {
        RecordingTriggerTypes.init();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (CLIP_KEYBIND.wasPressed()) {
                if (currentDatabaseHandler != null) {
                    RecordingTriggerTypes.MANUAL.trigger();
                }
			}
        });
        ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
        AttackEntityCallback.EVENT.register(RecordingTriggerTypes.ATTACK_ENTITY);
    }
}