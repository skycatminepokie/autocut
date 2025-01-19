package com.skycatdev.autocut.config;

import com.skycatdev.autocut.database.ClipType;
import com.skycatdev.autocut.trigger.RecordingTriggers;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Config {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("autocut");
    private static final Path CLIP_TYPES_PATH = CONFIG_PATH.resolve("clipTypes");
    private static final File EXPORT_CONFIG_FILE = CONFIG_PATH.resolve("export.json").toFile();
    private ArrayList<ClipType> clipTypes; // TODO: Serialization
    private ExportConfig exportConfig; // TODO: Serialization

    public ExportConfig getExportConfig() {
        return exportConfig;
    }

    public ArrayList<ClipType> getClipTypes() {
        return clipTypes;
    }

    public Config(ArrayList<ClipType> clipTypes, ExportConfig exportConfig) {
        this.clipTypes = clipTypes;
        this.exportConfig = exportConfig;
    }

    public static Config loadOrDefault() {
        File @Nullable[] clipTypeFiles = CLIP_TYPES_PATH.toFile().listFiles((file) -> !file.isFile());
        ArrayList<ClipType> clipTypes;
        if (clipTypeFiles != null) {
			clipTypes = new ArrayList<>(clipTypeFiles.length);
			for (File file : clipTypeFiles) {
				try {
					@Nullable ClipType clipType = ClipType.load(file);
                    if (clipType != null) {
                        clipTypes.add(clipType);
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("listFiles somehow found non-existent files?", e);
                }
            }
        } else {
            clipTypes = new ArrayList<>();
            // clipTypes.add(new ClipType(RecordingTriggers.MANUAL_TRIGGER, null, 30000, 100, true, false, ExportGroupingMode.INDIVIDUAL));
            clipTypes.add(new ClipType(RecordingTriggers.MANUAL_TRIGGER, RecordingTriggers.ATTACK_ENTITY_TRIGGER, 100, 100, true, false, ExportGroupingMode.INDIVIDUAL));
            // TODO: add defaults
        }
        return new Config(clipTypes, ExportConfig.readOrDefault(EXPORT_CONFIG_FILE));
    }

}
