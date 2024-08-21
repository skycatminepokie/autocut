package com.skycatdev.autocut;

import io.obswebsocket.community.client.OBSRemoteController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;

public class AutocutClient implements ClientModInitializer {
	@Nullable public static OBSRemoteController controller = null;
	@Nullable public static Recorder currentRecorder = null;
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(AutocutCommandHandler::register);
		ClientPlayerBlockBreakEvents.AFTER.register(((world, player, pos, state) -> {
			if (currentRecorder != null) {
				long time = System.currentTimeMillis();
				currentRecorder.addClip(new Clip(time - 250, time + 250, RecordingElementTypes.BREAK_BLOCK, "Broke " + state.getBlock().getName().toString())); // TODO: Localize
			}
		}));
		AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
			if (currentRecorder != null && entity != null) {
				long time = System.currentTimeMillis();
				currentRecorder.addClip(new Clip(time - 250, time +250, RecordingElementTypes.ATTACK_ENTITY, "Attacked " + entity.getName())); // TODO: Type and description
			}
			return ActionResult.PASS;
		}));
	}
}