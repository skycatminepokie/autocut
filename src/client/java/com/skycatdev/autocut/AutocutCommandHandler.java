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
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.IOException;
import java.sql.SQLException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutocutCommandHandler {

    private static final long DEFAULT_CLIP_LENGTH = 30000; // 30 seconds
    public static final int DEFAULT_PORT = 4455;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3;
    public static final String DEFAULT_HOST = "localhost";

    private static int connectPasswordCommand(CommandContext<FabricClientCommandSource> context) {
        String password = StringArgumentType.getString(context, "password");
        AutocutClient.controller = OBSRemoteController.builder()
                .host(DEFAULT_HOST)
                .port(DEFAULT_PORT)
                .password(password)
                .connectionTimeout(DEFAULT_CONNECTION_TIMEOUT)
                .lifecycle()
                .onReady(() -> {
                    context.getSource().sendFeedback(Text.of("OBS connected")); // TODO: Localize
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
            assert AutocutClient.currentRecordingHandler == null; // TODO: Error handling
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Recording started")); // TODO: Localize
            try {
                AutocutClient.currentRecordingHandler = new RecordingHandler();
            } catch (SQLException | IOException e) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Failed to start autocut: ").copy().append(Text.of("Exception").copy().setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(e.getLocalizedMessage())))))); // TODO: Localize and unawfulify
            }
        } else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Recording ended.")); // TODO: Localize
                if (AutocutClient.currentRecordingHandler == null) {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Warning: Recording was not started in autocut - no recording is saved.")); // TODO: Check at connect and warn // TODO: Localize
                } else {
                    AutocutClient.currentRecordingHandler.onRecordingEnded(recordStateChangedEvent.getOutputPath());
                }
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
                .build();
        var finishFfmpeg = argument("ffmpeg", StringArgumentType.string())
                .executes(AutocutCommandHandler::finish) // WARN: Debug only
                .build();
        //@formatter:off
        dispatcher.getRoot().addChild(autocut);
        autocut.addChild(connect);
            connect.addChild(connectPassword);
        autocut.addChild(finish);
            finish.addChild(finishFfmpeg);
        autocut.addChild(clip);
        //@formatter:on
    }

    private static int finish(CommandContext<FabricClientCommandSource> context) {
        assert AutocutClient.currentRecordingHandler != null;
        AutocutClient.currentRecordingHandler.export(StringArgumentType.getString(context, "ffmpeg"));
        AutocutClient.currentRecordingHandler = null;
        return Command.SINGLE_SUCCESS;
    }

    private static int makeClip(CommandContext<FabricClientCommandSource> context) {
        if (AutocutClient.currentRecordingHandler != null) {
            long time = System.currentTimeMillis();
            AutocutClient.currentRecordingHandler.addClip(new Clip(time - DEFAULT_CLIP_LENGTH, time, RecordingElementTypes.DEBUG, "Debug"));
            context.getSource().sendFeedback(Text.of("Clipped!")); // TODO: Localize
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("Not recording.")); // TODO: Localize
        return 0;
    }


}
