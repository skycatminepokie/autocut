package com.skycatdev.autocut.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Utils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ExportConfig {
    public static final String DEFAULT_FORMAT = "mp4";
    public static final String DEFAULT_NAME_FORMAT = "cut{ORIGINAL}";
    public static Codec<ExportConfig> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("format").forGetter(ExportConfig::getFormat),
            Codec.STRING.fieldOf("nameFormat").forGetter(ExportConfig::getNameFormat)
    ).apply(instance, ExportConfig::new));
    /**
     * The file format to export the video to (eg .mp4, .mkv)
     */
    public String format; // TODO use
    /**
     * The format name of the exported file. For example, "cut_{ORIGINAL}".
     */
    public String nameFormat; // TODO use

    public ExportConfig(String format, String nameFormat) {
        this.format = format;
        this.nameFormat = nameFormat;
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

    public String getExportName(String recordingName, long clips) {
        return nameFormat.trim()
                .replaceAll("\\{ORIGINAL}", recordingName)
                .replaceAll("\\{CLIPS}", String.valueOf(clips))
                .replaceAll("\\\\", "")
                .replaceAll("\\.", "") + "." + format;
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
            return new ExportConfig(DEFAULT_FORMAT, DEFAULT_NAME_FORMAT);
        }
        try {
            return Utils.readFromJson(file, CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigCategory generateConfigCategory() { // TODO: Localize
        return ConfigCategory.createBuilder()
                .name(Text.of("Export"))
                .tooltip(Text.of("Configure the format videos are exported in"))
                .option(Option.<String>createBuilder()
                        .name(Text.of("Format"))
                        .description(OptionDescription.of(Text.of("The video format to export to, eg \"mp4\", \"mkv\"")))
                        .binding(DEFAULT_FORMAT, this::getFormat, this::setFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidFormat))
                        .build()
                )
                .option(Option.<String>createBuilder()
                        .name(Text.of("Output file"))
                        .description(OptionDescription.of(Text.of("The name of the file to export to, not including the extension (like \".mp4\").\nIt's recommended to include variables (see below), otherwise you'll start overwriting your videos.\n{ORIGINAL} will be replaced with the file name of the recording\n{CLIPS} will be replaced with the number of clips.\nBackslashes (\\) and periods (.) will be ignored.")))
                        .binding(DEFAULT_NAME_FORMAT, this::getNameFormat, this::setNameFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidNameFormat))
                        .build()
                )
                .build();
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

}
