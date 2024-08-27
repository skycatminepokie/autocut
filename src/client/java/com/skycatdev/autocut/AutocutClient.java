package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.Clip;
import com.skycatdev.autocut.clips.ClipTypes;
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
                    currentRecordingHandler.addClip(new Clip(time - 250, time, time + 250, ClipTypes.BREAK_BLOCK, "Broke " + state.getBlock().getName().toString(), null, null, null, null)); // TODO: Localize
                } catch (SQLException e) { // TODO
                    e.printStackTrace();
                }
            }
        }));
        AttackEntityCallback.EVENT.register(((player, world, hand, entity, hitResult) -> {
            if (currentRecordingHandler != null && entity != null) {
                long time = System.currentTimeMillis();
                try {
                    currentRecordingHandler.addClip(new Clip(time - 250, time, time + 250, ClipTypes.ATTACK_ENTITY, "Attacked " + entity.getName(), null, null, null, null)); // TODO: Type and description
                } catch (SQLException ignored) { // TODO
                }
            }
            return ActionResult.PASS;
        }));
    }
}