package com.skycatdev.autocut.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.Utils;
import com.skycatdev.autocut.database.ClipType;
import com.skycatdev.autocut.trigger.RecordingTriggerTypes;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Config {
    public static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("autocut.json").toFile();
    private final ArrayList<ClipType> clipTypes;
    private final ExportConfig exportConfig;
    public static final Codec<Config> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClipType.CODEC.listOf().xmap(ArrayList::new, list -> list).fieldOf("clipTypes").forGetter(Config::getClipTypes),
            ExportConfig.CODEC.fieldOf("exportConfig").forGetter(Config::getExportConfig)
    ).apply(instance, Config::new));
    /**
     * If the top-level config is dirty (needs to be saved)
     */
    protected boolean isDirty = false;

    /**
     * @return If the config or any of its parts are dirty (need to be saved)
     */
    public boolean isDirty() {
        return  isDirty || exportConfig.isDirty();
    }

    public void markDirty() {
        isDirty = true;
    }

    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public ExportConfig getExportConfig() {
        return exportConfig;
    }

    public ArrayList<ClipType> getClipTypes() {
        return clipTypes;
    }

    public void save() throws IOException {
        Utils.saveToJson(CONFIG_FILE, CODEC, this);
    }

    public Config(ArrayList<ClipType> clipTypes, ExportConfig exportConfig) {
        this.clipTypes = clipTypes;
        this.exportConfig = exportConfig;
    }

    public static Config loadOrDefault() {
        @Nullable Config read = null;
		try {
			read = Utils.readFromJson(CONFIG_FILE, CODEC);
		} catch (IOException ignored) {
		}

        if (read == null) {
            Autocut.LOGGER.warn("Using default config");
            // TODO: Add more default ClipTypes
            ArrayList<ClipType> defaultClipTypes = new ArrayList<>();
            defaultClipTypes.add(new ClipType(RecordingTriggerTypes.MANUAL.makeDefault(), null, 30000, 5000, true, false, ExportGroupingMode.INDIVIDUAL));
            return new Config(defaultClipTypes, new ExportConfig());
        }
        return read;
    }

}
