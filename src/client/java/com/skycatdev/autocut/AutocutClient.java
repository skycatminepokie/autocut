package com.skycatdev.autocut;

import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import org.jetbrains.annotations.Nullable;

public class AutocutClient implements ClientModInitializer {
	@Nullable public static OBSRemoteController controller = null;
	@Nullable public static Recorder currentRecorder = null;
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
	}
}