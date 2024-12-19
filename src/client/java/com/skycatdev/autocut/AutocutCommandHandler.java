package com.skycatdev.autocut;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.skycatdev.autocut.record.ObsHandler;
import com.skycatdev.autocut.record.RecordingManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.io.File;
import java.sql.SQLException;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AutocutCommandHandler {

    // Exceptions are in the form of COMMAND_PATH_REASON_EXCEPTION
    private static final SimpleCommandExceptionType FINISH_DATABASE_DOES_NOT_EXIST_EXCEPTION = new SimpleCommandExceptionType(() -> Text.translatable("autocut.command.autocut.finish.database.fail.databaseDoesNotExist").getString());
    private static final SimpleCommandExceptionType FINISH_NO_RECORDING_EXCEPTION = new SimpleCommandExceptionType(() -> Text.translatable("autocut.command.autocut.finish.fail.noRecording").getString());
    private static final SimpleCommandExceptionType CONNECT_ALREADY_CONNECTED_EXCEPTION = new SimpleCommandExceptionType(() -> Text.translatable("autocut.command.autocut.connect.password.fail.alreadyConnected").getString());

    private static int connectPasswordCommand(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        String password = StringArgumentType.getString(context, "password");
        if (!ObsHandler.hasController()) {
            new Thread(() -> ObsHandler.createConnection(password), "Autocut OBS Connection Thread").start();
            return Command.SINGLE_SUCCESS;
        }
        throw CONNECT_ALREADY_CONNECTED_EXCEPTION.create();
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
        var finishDatabaseEdl = literal("edl")
                .executes(AutocutCommandHandler::finishDatabaseEdl)
                .build();
        //@formatter:off
        dispatcher.getRoot().addChild(autocut);
        autocut.addChild(connect);
            connect.addChild(connectPassword);
        autocut.addChild(finish);
            finish.addChild(finishDatabase);
                finishDatabase.addChild(finishDatabaseEdl);
        //@formatter:on
    }

    private static int finishDatabaseEdl(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        try {
            File database = new File(StringArgumentType.getString(context, "database"));
            if (!database.exists()) {
                throw FINISH_DATABASE_DOES_NOT_EXIST_EXCEPTION.create();
            }
            RecordingManager recordingManager = RecordingManager.fromDatabase(database);
            recordingManager.exportEdl();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Command.SINGLE_SUCCESS;
    }

}
