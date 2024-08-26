package com.skycatdev.autocut;

import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
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
                    currentRecordingHandler.addClip(new Clip(time - 250, time + 250, RecordingElementTypes.BREAK_BLOCK, "Broke " + state.getBlock().getName().toString())); // TODO: Localize
                } catch (SQLException e) { // TODO
					e.printStackTrace();
                }
            }
		}));
		AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
			if (currentRecordingHandler != null && entity != null) {
				long time = System.currentTimeMillis();
                try {
                    currentRecordingHandler.addClip(new Clip(time - 250, time +250, RecordingElementTypes.ATTACK_ENTITY, "Attacked " + entity.getName())); // TODO: Type and description
                } catch (SQLException ignored) { // TODO
                }
            }
			return ActionResult.PASS;
		}));
	}
}