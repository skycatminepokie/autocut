package com.skycatdev.autocut.datagen;

import com.skycatdev.autocut.Autocut;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class AutocutEnglishLangProvider extends FabricLanguageProvider {
    public static final String YACL_PREFIX = Autocut.MOD_ID + ".yacl";

    //? if >=1.20.5 {
    protected AutocutEnglishLangProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataGenerator, "en_us", registryLookup);
    }
    //?} else {
    /*protected AutocutEnglishLangProvider(FabricDataOutput dataGenerator) {
        super(dataGenerator, "en_us");
    }
    *///?}

    @Override
    //? if >=1.20.5 {
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder tb) {
    //?} else {
    /*public void generateTranslations(TranslationBuilder tb) {
    *///?}
        tb.add("key.category.autocut.autocut", "Autocut");
        tb.add("key.autocut.clip", "Clip");
        tb.add("autocut.yacl.title", "Autocut configuration menu");
        tb.add(YACL_PREFIX + ".category.clips", "Clips");
        tb.add(YACL_PREFIX + ".category.clips.tooltip", "Configure how sections of video are saved. Note that these settings only apply for future clips.");

        // TODO: Prefix with "clip."
        addOptionGroup("place_block_clip", "Place block", "When you place a block", tb);
        addOptionGroup("shoot_player_clip", "Shoot player", "When you shoot a player. Note that some servers will \"fake\" this in order to play the sound that comes with it. On Hypixel, you'll notice this when you join a game. You can filter these out if you like - it seems to only happen when you're at x=8.5, z=8.5.", tb);
        addOptionGroup("use_item_clip", "Use item", "When you use (right-click with) an item. This happens at the beginning of the click, and so it is not guaranteed to include the whole click if the button is held.", tb);
        addOptionGroup("break_block_clip", "Break block", "When you break a block. This happens at the end, so it's not guaranteed that the whole breaking time will be included.", tb);
        addOptionGroup("attack_entity_clip", "Attack entity", "When you attempt to attack an entity, including a player.", tb);
        addOptionGroup("death_clip", "Die", "When you die.", tb);
        addOptionGroup("take_damage_clip", "Take damage", "Whenever you take damage from any source.", tb);
        addOptionGroup("manual_clip", "Manual", "When you press the keybind.\nDefault: %s\nCurrent: %s", tb);
        addOptionGroup("receive_player_message_clip", "Receive player message", "When you receive a chat message from a player. This may be a direct message or a regular one. This may be triggered as a system message on some servers.", tb);
        addOptionGroup("receive_server_message_clip", "Receive server message", "When you receive a chat message from the server or part of the game. Player messages may trigger this on some servers.", tb);
        addOptionGroup("not_in_world", "Not in world", "When you're in a world, or paused in single player.", tb);


        addOption("generic.start_offset", "Default start offset", "How many milliseconds before the event should be counted as part of the clip.", tb);
        addOption("generic.end_offset", "Default end offset", "How many milliseconds after the event should be counted as part of the clip.", tb);
        addOption("generic.should_record", "Enable record", "Whether this event should be recorded.", tb);
        addOption("generic.active", "Active", "Whether this event should be exported in the final record.", tb);
        addOption("generic.inverse", "Invert new clips", "If new clips should be the opposite of clips - the footage they cover will NOT be exported.", tb);
        addOption("generic.grouping_mode", "Grouping mode", "What clips this group should be exported with.", tb);
        addOption("take_damage_clip.damage_precision", "Damage precision", "How many decimal points of precision to record damage at.", tb);

        tb.add(YACL_PREFIX + ".category.export", "Export");
        tb.add(YACL_PREFIX + ".category.export.tooltip", "Configure the format videos are exported in");

        addOption("export.format", "Format", "The video format to export to, eg \"mp4\", \"mkv\"", tb);
        addOption("export.fileFormat", "Output file", """
                The name of the file to export to, not including the extension (like ".mp4").
                It's recommended to include variables (see below), otherwise you'll start overwriting your videos.
                {ORIGINAL} will be replaced with the file name of the record
                {CLIPS} will be replaced with the number of clips.
                Backslashes (\\) and periods (.) will be ignored.""", tb);
        addOption("export.existingFiles", "Existing files", "Defines what happens when trying to export something with the same name as an already existing file.", tb);
        addOption("export.ffmpegFolder", "FFmpeg Folder", "The folder where FFmpeg and FFprobe are located. Leave blank for auto-detect (doesn't work on Linux).", tb);
        tb.add("autocut.yacl.export.existingFiles.keep", "Keep both");
        tb.add("autocut.yacl.export.existingFiles.overwrite", "Overwrite old");


        addText("record.connect.success", "OBS connected.", tb);
        addText("record.disconnect", "OBS disconnected", tb);
        addText("record.start.success", "Recording started.", tb);
        addText("record.start.fail", "Failed to start autocut", tb);
        addText("record.connect.alreadyRecording", "Warning: You started recording before connecting Autocut. Autocut will not record until you restart recording.", tb);
        addText("record.end.success", "Recording ended.", tb);
        addText("record.end.fail.notStarted", "Warning: Recording was not started in autocut - no record is saved.", tb);
        addText("record.end.fail.sqlException", "Failed to save metadata for record (SQLException)", tb);
        addText("cutting.finishAll", "Finished cutting!", tb);
        addText("cutting.finish", "Finished cutting [%s/%s]", tb);
        addText("cutting.progress", "Cutting: %s%% [%s/%s]", tb);
        addText("cutting.start", "Preparing to cut...", tb);
        addText("cutting.progress.fail", "Something went wrong while exporting. Check your logs for more info.", tb);
        addText("cutting.fail", "Something went wrong while preparing to export. Check your logs for more info.", tb);
        addText("edl.start.fail", "Failed to get frame rate for recording. EDL export aborted.", tb);
        addText("edl.start.fail.probeFrameRate", "Failed to probe for frame rate. EDL export aborted.", tb);
        addText("edl.start.fail.noVideoStream", "Couldn't find a video stream. EDL export aborted.", tb);
        addText("edl.progress.fail", "Something went wrong while exporting. Check your logs for more info.", tb);
        addText("edl.finish", "Successfully exported to EDL.", tb);
        addText("exportGroupingMode.none", "Main file", tb);
        addText("exportGroupingMode.type", "Own type", tb);
        addText("exportGroupingMode.individual", "Separate", tb);

        addCommandMessage("autocut.finish.database", "fail.databaseDoesNotExist", "The given database does not exist.", tb);
        addCommandMessage("autocut.finish", "fail.noRecording", "No record found. Did you connect, start recording, then stop recording?", tb);
        addCommandMessage("autocut.connect.password", "fail.alreadyConnected", "Already connected.", tb);

    }

    private void addOption(String key, String optionName, String description, TranslationBuilder tb) {
        tb.add(YACL_PREFIX + "." + key, optionName);
        tb.add(YACL_PREFIX + "." + key + ".description", description);
    }

    private void addOptionGroup(String key, String groupName, String description, TranslationBuilder tb) {
        tb.add(YACL_PREFIX + "." + key, groupName);
        tb.add(YACL_PREFIX + "." + key + ".description", description);
    }

    private void addCommandMessage(String commandPath, String key, String text, TranslationBuilder tb) {
        addText("command." + commandPath + "." + key, text, tb);
    }

    private void addText(String path, String text, TranslationBuilder tb) {
        tb.add(Autocut.MOD_ID + "." + path, text);
    }
}
