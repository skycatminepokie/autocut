package com.skycatdev.autocut.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;

import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

public class ExportConfig {
    /**
     * The file format to export the video to (eg .mp4, .mkv)
     */
    public String format; // TODO use
    /**
     * The format name of the exported file. For example, "cut_{ORIGINAL}".
     */
    public String nameFormat; // TODO use

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
        try {
            Paths.get(nameFormat);
        } catch (InvalidPathException e) {
            return false;
        }
        return true;
    }

    public ConfigCategory generateConfigCategory() { // TODO: Localize
        return ConfigCategory.createBuilder()
                .name(Text.of("Export"))
                .tooltip(Text.of("Change how videos are cut"))
                .option(Option.<String>createBuilder()
                        .name(Text.of("Format"))
                        .description(OptionDescription.of(Text.of("The video format to export to, eg \"mp4\", \"mkv\"")))
                        .binding("mp4", this::getFormat, this::setFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidFormat))
                        .build()
                )
                .option(Option.<String>createBuilder()
                        .name(Text.of("Output file"))
                        .description(OptionDescription.of(Text.of("The name of the file to export to, not including the extension (like \".mp4\").\n{ORIGINAL} will be replaced with the file name of the recording\n{CLIPS} will be replaced with the number of clips.")))
                        .binding("cut{ORIGINAL}", this::getNameFormat, this::setNameFormat)
                        .controller((option) -> () -> new PredicatedStringController(option, ExportConfig::isValidNameFormat))
                        .build()
                )
                .build();
    }

    private String getFormat() {
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

}
