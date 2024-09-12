package com.skycatdev.autocut;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.skycatdev.autocut.clips.ClipBuilder;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.outputs.RecordStateChangedEvent;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutocutCommandHandler {

    /**
     * A clip made using debug methods. Ideally not used by the end user.
     */
    public static final Identifier DEBUG = Identifier.of(Autocut.MOD_ID, "debug");
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
            assert AutocutClient.currentRecordingManager == null; // TODO: Error handling
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Recording started")); // TODO: Localize
            try {
                AutocutClient.currentRecordingManager = new RecordingManager();
            } catch (SQLException | IOException e) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Failed to start autocut: ").copy().append(Text.of("Exception").copy().setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(e.getLocalizedMessage())))))); // TODO: Localize and unawfulify
            }
        } else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Recording ended.")); // TODO: Localize
                if (AutocutClient.currentRecordingManager == null) {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("Warning: Recording was not started in autocut - no recording is saved.")); // TODO: Check at connect and warn // TODO: Localize
                } else {
                    AutocutClient.currentRecordingManager.onRecordingEnded(recordStateChangedEvent.getOutputPath());
                }
            }
        }
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess ignoredCommandRegistryAccess) {
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
                .executes(AutocutCommandHandler::finish)
                .build();
        var finishDatabase = argument("database", StringArgumentType.string())
                .executes(AutocutCommandHandler::finishDatabase)
                .build();
        //@formatter:off
        dispatcher.getRoot().addChild(autocut);
        autocut.addChild(connect);
            connect.addChild(connectPassword);
        autocut.addChild(finish);
            finish.addChild(finishDatabase);
        autocut.addChild(clip);
        //@formatter:on
    }

    private static int finish(CommandContext<FabricClientCommandSource> context) {
        try {
            AutocutClient.currentRecordingManager.export();
        } catch (SQLException e) {
            throw new RuntimeException(e); // TODO: Error handling
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int finishDatabase(CommandContext<FabricClientCommandSource> context) {
        try {
            RecordingManager recordingManager = RecordingManager.fromDatabase(new File(StringArgumentType.getString(context, "database")));
            recordingManager.export();
        } catch (SQLException e) {
            throw new RuntimeException(e); // TODO: Error handling
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int makeClip(CommandContext<FabricClientCommandSource> context) {
        if (AutocutClient.currentRecordingManager != null) {
            long time = System.currentTimeMillis();
            try {
                AutocutClient.currentRecordingManager.addClip(new ClipBuilder(time - DEFAULT_CLIP_LENGTH, time, time, DEBUG)
                        .setDescription("Debug")
                        .build());
            } catch (SQLException ignored) { // TODO
            }
            context.getSource().sendFeedback(Text.of("Clipped!")); // TODO: Localize
            return Command.SINGLE_SUCCESS;
        }
        context.getSource().sendError(Text.of("Not recording.")); // TODO: Localize
        return 0;
    }


}
