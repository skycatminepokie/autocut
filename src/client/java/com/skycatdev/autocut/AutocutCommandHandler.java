package com.skycatdev.autocut;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.outputs.RecordStateChangedEvent;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutocutCommandHandler {

    private static int connectPasswordCommand(CommandContext<FabricClientCommandSource> context) {
        String password = StringArgumentType.getString(context, "password");
        AutocutClient.controller = OBSRemoteController.builder()
                .host("localhost")
                .port(4455)
                .password(password)
                .connectionTimeout(3)
                .lifecycle()
                .onReady(() -> {
                    context.getSource().sendFeedback(Text.of("OBS connected"));
                })
                .and()
                .autoConnect(true)
                .registerEventListener(RecordStateChangedEvent.class, AutocutCommandHandler::onRecordEventChanged)
                .build();
        //controller.connect();
        return 1;
    }

    private static void onRecordEventChanged(RecordStateChangedEvent recordStateChangedEvent) {
        // Looks like output path is null if stopping or starting, but is not null when stopped or started
        if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STARTED")) {
            assert AutocutClient.currentRecorder == null; // TODO: Error handling
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Recording started"));
            AutocutClient.currentRecorder = new Recorder();
        } else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                assert AutocutClient.currentRecorder != null; // TODO: Error handling
                AutocutClient.currentRecorder.onRecordingEnded(recordStateChangedEvent.getOutputPath());
            }
        }
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        var autocut = literal("autocut")
                .build();
        var connect = literal("connect")
                .build();
        var connectPassword = argument("password", StringArgumentType.word())
                .executes(AutocutCommandHandler::connectPasswordCommand)
                .build();
        var clip = literal("clip")
                .executes(AutocutCommandHandler::makeClip) // WARN: Debug only
                .build();
        var finish = literal("finish")
                .executes(AutocutCommandHandler::finish) // WARN: Debug only
                .build();
        //@formatter:off
        dispatcher.getRoot().addChild(autocut);
        autocut.addChild(connect);
            connect.addChild(connectPassword);
        autocut.addChild(finish);
        autocut.addChild(clip);
        //@formatter:on
    }

    private static int finish(CommandContext<FabricClientCommandSource> context) {
        assert AutocutClient.currentRecorder != null;
        AutocutClient.currentRecorder.export();
        // AutocutClient.currentRecorder = null; WARN TESTING TO REMOVE IT
        return Command.SINGLE_SUCCESS;
    }

    private static int makeClip(CommandContext<FabricClientCommandSource> context) {
        if (AutocutClient.currentRecorder != null) {
            long time = AutocutClient.currentRecorder.getRecordingTime();
            AutocutClient.currentRecorder.addClip(new Clip(time, time + 100, ClipTypes.DEBUG, "Debug"));
            context.getSource().sendFeedback(Text.of("Clipped!")); // TODO: Localize
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("Not recording.")); // TODO: Localize
        return 0;
    }


}
