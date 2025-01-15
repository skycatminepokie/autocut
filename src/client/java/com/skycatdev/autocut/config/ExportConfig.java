package com.skycatdev.autocut.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.Utils;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFprobe;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ExportConfig { // Warn: There's probably some race conditions in here with changing the config while exporting
    public static final String DEFAULT_FORMAT = "mp4";
    public static final String DEFAULT_NAME_FORMAT = "cut{ORIGINAL}";
    public static final boolean DEFAULT_KEEP_OLD = true;
    public static final Codec<ExportConfig> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("format").forGetter(ExportConfig::getFormat),
            Codec.STRING.fieldOf("nameFormat").forGetter(ExportConfig::getNameFormat),
            Codec.BOOL.fieldOf("keepOld").orElse(true).forGetter(ExportConfig::shouldKeepOld),
            Codec.STRING.optionalFieldOf("ffmpegFolder").xmap((opt) -> opt.orElse(""), Optional::of).forGetter(ExportConfig::getFFmpegFolder)
    ).apply(instance, ExportConfig::new));
    /**
     * The file format to export the video to (eg .mp4, .mkv)
     */
    public String format;
    /**
     * The format name of the exported file. For example, "cut_{ORIGINAL}".
     */
    public String nameFormat;
    /**
     * Whether old files should be kept (true, rename the current one) or overwritten (false)
     */
    public boolean keepOld;
    /**
     * The folder with FFmpeg.
     */
    public String ffmpegFolder;

    public String getFFmpegFolder() {
        return ffmpegFolder;
    }

    public FFmpeg getFFmpeg() throws IOException {
        if (!ffmpegFolder.isBlank()) {
            return new FFmpeg(Path.of(ffmpegFolder).resolve("ffmpeg").toString());
        }
        return new FFmpeg();
    }

    public FFprobe getFFprobe() throws IOException {
        if (!ffmpegFolder.isBlank()) {
            return new FFprobe(Path.of(ffmpegFolder).resolve("ffprobe").toString());
        }
        return new FFprobe();
    }

    public ExportConfig(String format, String nameFormat, boolean keepOld) {
        this(format, nameFormat, keepOld, "");
    }

    public ExportConfig(String format, String nameFormat, boolean keepOld, String ffmpegFolder) {
        this.format = format;
        this.nameFormat = nameFormat;
        this.keepOld = keepOld;
        this.ffmpegFolder = ffmpegFolder;
    }

    public static boolean isValidFormat(String format) {
        if (format.charAt(0) == '.') {
            format = format.substring(1);
        }
        if (format.isEmpty()) {
            return false;
        }
        for (int i = 0; i < format.length(); i++) {
            if (Character.isAlphabetic(format.charAt(i))) {
                continue;
            }
            try {
                Integer.parseInt(String.valueOf(i));
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean isValidNameFormat(String nameFormat) {
        nameFormat = nameFormat.trim();
        nameFormat = nameFormat.replaceAll("\\{ORIGINAL}", "ORIGINAL");
        nameFormat = nameFormat.replaceAll("\\{CLIPS}", "132");
        nameFormat = nameFormat.replaceAll("\\\\", "");
        try {
            Paths.get(nameFormat);
        } catch (InvalidPathException e) {
            return false;
        }
        return true;
    }

    /**
     * Reads an {@link ExportConfig} from a file, or returns a default.
     *
     * @param file The file to read from. It will be created if it does not exist.
     * @return The read config, or a default if the config cannot be found or is invalid
     */
    public static ExportConfig readOrDefault(File file) {
        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException();
            }
            return new ExportConfig(DEFAULT_FORMAT, DEFAULT_NAME_FORMAT, DEFAULT_KEEP_OLD);
        }
        try {
            @Nullable ExportConfig exportConfig = Utils.readFromJson(file, CODEC);
            if (exportConfig != null) {
                return exportConfig;
            }
        } catch (IOException e) {
            Autocut.LOGGER.warn("Failed to load old export config, making a default one (instead of crashing)");
        }
        return new ExportConfig(DEFAULT_FORMAT, DEFAULT_NAME_FORMAT, DEFAULT_KEEP_OLD);
    }

    private void setFfmpegFolder(String path) {
        ffmpegFolder = path;
    }

    public synchronized File getFFmpegExportFile(File original, int clips) {
        return getExportFile(original, clips, format);
    }

    public synchronized File getExportFile(File original, int clips, String format) {
        String recordingName = original.getName();
        String firstName = nameFormat.trim()
                .replaceAll("\\{ORIGINAL}", recordingName)
                .replaceAll("\\{CLIPS}", String.valueOf(clips))
                .replaceAll("\\\\", "")
                .replaceAll("\\.", "");
        File export = original.toPath().resolveSibling(firstName + "." + format).toFile();
        // if (ConfigHandler.getExportConfig().shouldKeepOld()) {
        //     int i = 0;
        //     while (export.exists()) { // TODO: Cache this? Few file system queries.
        //         i++;
        //         export = original.toPath().resolveSibling(firstName + i + "." + format).toFile();
        //     }
        // }
        return export;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getNameFormat() {
        return nameFormat;
    }

    public void setNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
    }

    public void saveToFile(File file) throws IOException {
        Utils.saveToJson(file, CODEC, this);
    }

    public void setKeepOld(boolean keepOld) {
        this.keepOld = keepOld;
    }

    public boolean shouldKeepOld() {
        return keepOld;
    }

}
