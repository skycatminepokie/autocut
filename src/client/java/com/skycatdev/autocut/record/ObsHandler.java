package com.skycatdev.autocut.record;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.AutocutClient;
import com.skycatdev.autocut.database.DatabaseHandler;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.outputs.RecordStateChangedEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class ObsHandler { // All this is likely on a thread other than the main client thread.
    public static final int DEFAULT_PORT = 4455;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3;
    public static final String DEFAULT_HOST = "localhost";
    @Nullable private static OBSRemoteController controller = null;

    public static void createConnection(String password) {
        controller = OBSRemoteController.builder()
                .host(DEFAULT_HOST)
                .port(DEFAULT_PORT)
                .password(password)
                .connectionTimeout(DEFAULT_CONNECTION_TIMEOUT)
                .lifecycle()
                .onReady(ObsHandler::onReady)
                .onClose((closeCode) -> {
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.disconnect"));
                    controller = null;
                })
                .and()
                .autoConnect(true)
                .registerEventListener(RecordStateChangedEvent.class, ObsHandler::onRecordEventChanged)
                .build();
    }

    public static boolean hasController() {
        return controller != null;
    }

    private static void onReady() {
        AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.connect.success"));
        assert controller != null;
        controller.getRecordStatus((response) -> {
            if (response.isSuccessful()) {
                if (response.getOutputActive()) {
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.connect.alreadyRecording"));
                }
            } else {
                Autocut.LOGGER.warn("Unsuccessful trying to request recording status. Something might be wrong, but it could just be fine.");
            }
        });
    }

    private static void onRecordEventChanged(RecordStateChangedEvent recordStateChangedEvent) { // Not on client thread
        // Looks like output path is null if stopping or starting, but is not null when stopped or started
        if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STARTED")) {
            AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.start.success"));
			try {
				AutocutClient.currentDatabaseHandler = DatabaseHandler.makeNew(System.currentTimeMillis());
            } catch (IOException e) {
                AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.start.fail"));
            }
		} else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.end.success"));
                if (AutocutClient.currentDatabaseHandler == null) {
                    AutocutClient.sendMessageOnClientThread(Text.translatable("autocut.record.end.fail.notStarted"));
                } else {
					try {
						AutocutClient.currentDatabaseHandler.get().onRecordingEnded(recordStateChangedEvent.getOutputPath());
					} catch (InterruptedException | ExecutionException e) {
                        // Probably won't happen
						throw new RuntimeException(e);
					}
				}
            }
        }
    }

    private static Style styleHoverException(Exception e) {
        return Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(e.getLocalizedMessage())));
    }
}
