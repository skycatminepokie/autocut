package com.skycatdev.autocut.config;

import com.skycatdev.autocut.Autocut;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * What group a clip should be exported with
 */
public enum ExportGroupingMode {
    /**
     * Keep it with all the other clips
     */
    NONE(Identifier.of(Autocut.MOD_ID, "none"), Text.translatable("autocut.exportGroupingMode.none")),
    /**
     * Put it in a file with the things of its type
     */
    TYPE(Identifier.of(Autocut.MOD_ID, "type"), Text.translatable("autocut.exportGroupingMode.type")),
    /**
     * Give it its own file
     */
    INDIVIDUAL(Identifier.of(Autocut.MOD_ID, "individual"), Text.translatable("autocut.exportGroupingMode.individual"));

    private final Identifier id;
    private final Text name;
    ExportGroupingMode(Identifier id, Text displayName) {
        this.id = id;
        this.name = displayName;
    }

    public static @Nullable ExportGroupingMode fromId(Identifier id) { // Maybe bad? Idk
        for (ExportGroupingMode mode : ExportGroupingMode.values()) {
            if (mode.getId().equals(id)) {
                return mode;
            }
        }
        return null;
    }

    public Identifier getId() {
        return id;
    }

    public Text getName() {
        return name;
    }
}
