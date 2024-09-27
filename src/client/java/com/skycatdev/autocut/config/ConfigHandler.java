package com.skycatdev.autocut.config;

import com.mojang.serialization.Codec;
import com.skycatdev.autocut.Utils;
import com.skycatdev.autocut.clips.ClipType;
import com.skycatdev.autocut.clips.ClipTypeEntry;
import com.skycatdev.autocut.clips.ClipTypes;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Supplier;

public class ConfigHandler {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autocut");
    private static final File EXPORT_CONFIG_FILE = CONFIG_PATH.resolve("export.json").toFile();
    private static final ExportConfig EXPORT_CONFIG = ExportConfig.readOrDefault(EXPORT_CONFIG_FILE);

    protected static @NotNull Path getClipTypeConfigPath(Identifier typeId) {
        return CONFIG_PATH.resolve(typeId.getNamespace()).resolve(typeId.getPath() + ".json");
    }

    public static @NotNull ConfigScreenFactory<Screen> getConfigScreenFactory() {
        ConfigCategory.Builder clipsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("autocut.yacl.category.clips"))
                .tooltip(Text.translatable("autocut.yacl.category.clips.tooltip"));
        ClipTypes.CLIP_TYPE_REGISTRY.forEach((clipType) -> clipsCategory.group(clipType.clipType().buildOptionGroup()));
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("autocut.yacl.title"))
                .category(clipsCategory.build())
                .category(getExportConfig().generateConfigCategory())
                .save(ConfigHandler::saveAll)
                .build().generateScreen(parent);
    }

    public static ExportConfig getExportConfig() {
        return EXPORT_CONFIG;
    }

    public static void saveAll() {
        saveAllClipTypes();
        try {
            getExportConfig().saveToFile(EXPORT_CONFIG_FILE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused") // I want it around just in case
    public static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec) {
        File configFile = getClipTypeConfigPath(typeId).toFile();
        return readClipType(typeId, typeCodec, configFile);
    }

    public static <T extends ClipType> T readClipType(Identifier typeId, Codec<T> typeCodec, File configFile) {
        try {
            return Utils.readFromJson(configFile, typeCodec);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize clipType of id " + typeId + ". For a quick fix, try deleting the config file " + configFile.getAbsolutePath() + ". You may lose configs.");
        }
    }

    public static <T extends ClipType> T readClipTypeOrDefault(Identifier typeId, Codec<T> typeCodec, Supplier<T> defaultSupplier) {
        T clipType;
        Path configPath = getClipTypeConfigPath(typeId);
        File configFile = configPath.toFile();
        if (!configFile.exists()) {
            clipType = defaultSupplier.get();
            //noinspection ResultOfMethodCallIgnored
            configPath.getParent().toFile().mkdirs();
            try {
                //noinspection ResultOfMethodCallIgnored
                configFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            saveClipType(typeCodec, clipType);
        } else {
            clipType = readClipType(typeId, typeCodec);
        }
        return clipType;
    }

    protected static void saveAllClipTypes() {
        ClipTypes.CLIP_TYPE_REGISTRY.forEach(ConfigHandler::saveClipType);
    }

    public static <T extends ClipType> void saveClipType(ClipTypeEntry<T> clipTypeEntry) {
        saveClipType(clipTypeEntry.codec(), clipTypeEntry.clipType());
    }

    public static <T extends ClipType> void saveClipType(Codec<T> typeCodec, T clipType) {
        File configFile = ConfigHandler.getClipTypeConfigPath(clipType.getId()).toFile();
        try {
            Utils.saveToJson(configFile, typeCodec, clipType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save clip type " + clipType.getId() + ".", e);
        }
    }
}
