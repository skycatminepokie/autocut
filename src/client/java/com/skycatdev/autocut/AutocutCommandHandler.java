package com.skycatdev.autocut;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.obswebsocket.community.client.OBSRemoteController;
import io.obswebsocket.community.client.message.event.outputs.RecordStateChangedEvent;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutocutCommandHandler {

    public static final int DEFAULT_PORT = 4455;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 3;
    public static final String DEFAULT_HOST = "localhost";
    // Exceptions are in the form of COMMAND_PATH_REASON_EXCEPTION
    private static final SimpleCommandExceptionType FINISH_DATABASE_DOES_NOT_EXIST_EXCEPTION = new SimpleCommandExceptionType(() -> Text.stringifiedTranslatable("autocut.command.autocut.finish.database.fail.databaseDoesNotExist").getString());
    private static final SimpleCommandExceptionType FINISH_NO_RECORDING_EXCEPTION = new SimpleCommandExceptionType(() -> Text.stringifiedTranslatable("autocut.command.autocut.finish.fail.noRecording").getString());

    private static int connectPasswordCommand(CommandContext<FabricClientCommandSource> context) {
        String password = StringArgumentType.getString(context, "password");
        AutocutClient.controller = OBSRemoteController.builder()
                .host(DEFAULT_HOST)
                .port(DEFAULT_PORT)
                .password(password)
                .connectionTimeout(DEFAULT_CONNECTION_TIMEOUT)
                .lifecycle()
                .onReady(() -> {
                    context.getSource().sendFeedback(Text.translatable("autocut.recording.connect.success"));
                })
                .and()
                .autoConnect(true)
                .registerEventListener(RecordStateChangedEvent.class, AutocutCommandHandler::onRecordEventChanged)
                .build();
        //controller.connect();
        return 1;
    }

    private static int finish(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            if (AutocutClient.currentRecordingManager == null) {
                throw FINISH_NO_RECORDING_EXCEPTION.create();
            }
            AutocutClient.currentRecordingManager.export();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int finishDatabase(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            File database = new File(StringArgumentType.getString(context, "database"));
            if (!database.exists()) {
                throw FINISH_DATABASE_DOES_NOT_EXIST_EXCEPTION.create();
            }
            RecordingManager recordingManager = RecordingManager.fromDatabase(database);
            recordingManager.export();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void onRecordEventChanged(RecordStateChangedEvent recordStateChangedEvent) {
        // Looks like output path is null if stopping or starting, but is not null when stopped or started
        if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STARTED")) {
            assert AutocutClient.currentRecordingManager == null; // TODO: Error handling
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("autocut.recording.start.success"));
            try {
                AutocutClient.currentRecordingManager = new RecordingManager();
            } catch (SQLException | IOException e) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("autocut.recording.start.fail").setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(e.getLocalizedMessage()))))); // TODO: make a function for the supplier
            }
        } else {
            if (recordStateChangedEvent.getOutputState().equals("OBS_WEBSOCKET_OUTPUT_STOPPED")) {
                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("autocut.recording.end.success"));
                if (AutocutClient.currentRecordingManager == null) {
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.translatable("autocut.recording.end.fail.notStarted")); // TODO: Check at connect and warn
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
        //@formatter:on
    }

}
