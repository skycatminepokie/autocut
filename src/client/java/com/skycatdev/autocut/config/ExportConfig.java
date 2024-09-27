package com.skycatdev.autocut.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Utils;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import net.minecraft.text.Text;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ExportConfig {
    public static final String DEFAULT_FORMAT = "mp4";
    public static final String DEFAULT_NAME_FORMAT = "cut{ORIGINAL}";
    public static final boolean DEFAULT_KEEP_OLD = true;
    public static final Codec<ExportConfig> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("format").forGetter(ExportConfig::getFormat),
            Codec.STRING.fieldOf("nameFormat").forGetter(ExportConfig::getNameFormat),
            Codec.BOOL.fieldOf("keepOld").orElse(true).forGetter(ExportConfig::shouldKeepOld)
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

    public ExportConfig(String format, String nameFormat, boolean keepOld) {
        this.format = format;
        this.nameFormat = nameFormat;
        this.keepOld = keepOld;
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
            return Utils.readFromJson(file, CODEC);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigCategory generateConfigCategory() {
        return ConfigCategory.createBuilder()
                .name(Text.translatable("autocut.yacl.category.export"))
                .tooltip(Text.translatable("autocut.yacl.category.export.tooltip"))
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("autocut.yacl.export.format"))
                        .description(OptionDescription.of(Text.translatable("autocut.yacl.export.format.description")))
                        .binding(DEFAULT_FORMAT, this::getFormat, this::setFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidFormat))
                        .build()
                )
                .option(Option.<String>createBuilder()
                        .name(Text.translatable("autocut.yacl.export.fileFormat"))
                        .description(OptionDescription.of(Text.translatable("autocut.yacl.export.fileFormat.description")))
                        .binding(DEFAULT_NAME_FORMAT, this::getNameFormat, this::setNameFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidNameFormat))
                        .build()
                )
                .option(Option.<Boolean>createBuilder() // TODO: Localize
                        .name(Text.of("Existing files"))
                        .description(OptionDescription.of(Text.of("Defines what happens when trying to export something with the same name as an already existing file.")))
                        .binding(DEFAULT_KEEP_OLD, this::shouldKeepOld, this::setKeepOld)
                        .controller(option -> BooleanControllerBuilder.create(option)
                                .formatValue(bool -> bool ? Text.of("Keep both") : Text.of("Overwrite old"))
                                .coloured(true)
                        )
                        .build()
                )
                .build();
    }

    public String getExportName(String recordingName, long clips) {
        return nameFormat.trim()
                       .replaceAll("\\{ORIGINAL}", recordingName)
                       .replaceAll("\\{CLIPS}", String.valueOf(clips))
                       .replaceAll("\\\\", "")
                       .replaceAll("\\.", "") + "." + format;
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
