package com.skycatdev.autocut;

import com.skycatdev.autocut.database.DatabaseHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;


public class AutocutClient implements ClientModInitializer {
    public static final KeyBinding CLIP_KEYBIND = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.autocut.clip", InputUtil.UNKNOWN_KEY.getCode(), "key.category.autocut.autocut"));
    @Nullable public static DatabaseHandler currentDatabaseHandler = null;

    /**
     * Utility for consistently sending messages to the player from another thread.
     */
    public static void sendMessageOnClientThread(Text text) {
        MinecraftClient client = MinecraftClient.getInstance();
        client.send(() -> client.inGameHud.getChatHud().addMessage(text));
    }

    @Override
    public void onInitializeClient() {

    }
}