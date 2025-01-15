package com.skycatdev.autocut.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;

public class ConfigHandler {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autocut");
    private static final File EXPORT_CONFIG_FILE = CONFIG_PATH.resolve("export.json").toFile();
    private static final ExportConfig EXPORT_CONFIG = ExportConfig.readOrDefault(EXPORT_CONFIG_FILE);

    public static ExportConfig getExportConfig() {
        return EXPORT_CONFIG;
    }

}
