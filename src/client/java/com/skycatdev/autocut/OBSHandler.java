package com.skycatdev.autocut;

import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.outputs.RecordStateChangedEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.IOException;
import java.sql.SQLException;

public class OBSHandler { // All this is likely on a thread other than the main client thread.
    public static final int DEFAULT_PORT = 4455;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3;
    public static final String DEFAULT_HOST = "localhost";

    static void createConnection(String password) {
        AutocutClient.controller = OBSRemoteController.builder()
                .host(DEFAULT_HOST)
                .port(DEFAULT_PORT)
                .password(password)
                .connectionTimeout(DEFAULT_CONNECTION_TIMEOUT)
                .lifecycle()
                .onReady(() -> sendMessageOnClientThread(MinecraftClient.getInstance(), Text.translatable("autocut.recording.connect.success")))
                .and()
                .autoConnect(true)
                .registerEventListener(RecordStateChangedEvent.class, OBSHandler::onRecordEventChanged)
                .build();
    }

    private static void onRecordEventChanged(RecordStateChangedEvent recordStateChangedEvent) { // Not on client thread
        // Looks like output path is null if stopping or starting, but is not null when stopped or started
        if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STARTED")) {
            assert AutocutClient.currentRecordingManager == null; // TODO: Error handling
            MinecraftClient client = MinecraftClient.getInstance();
            sendMessageOnClientThread(client, Text.translatable("autocut.recording.start.success"));
            try {
                AutocutClient.currentRecordingManager = new RecordingManager();
            } catch (SQLException | IOException e) {
                sendMessageOnClientThread(client, Text.translatable("autocut.recording.start.fail").setStyle(styleHoverException(e)));
            }
        } else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                MinecraftClient client = MinecraftClient.getInstance();
                sendMessageOnClientThread(client, Text.translatable("autocut.recording.end.success"));
                if (AutocutClient.currentRecordingManager == null) {
                    sendMessageOnClientThread(client, Text.translatable("autocut.recording.end.fail.notStarted")); // TODO: Check at connect and warn
                } else {
                    AutocutClient.currentRecordingManager.onRecordingEnded(recordStateChangedEvent.getOutputPath());
                }
            }
        }
    }

    private static void sendMessageOnClientThread(MinecraftClient client, Text text) {
        client.send(() -> client.inGameHud.getChatHud().addMessage(text));
    }

    private static Style styleHoverException(Exception e) {
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(e.getLocalizedMessage())));
    }
}
