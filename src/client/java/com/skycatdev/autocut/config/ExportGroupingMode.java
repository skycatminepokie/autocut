package com.skycatdev.autocut.config;

import com.skycatdev.autocut.Autocut;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * What group a clip should be exported with
 */
public enum ExportGroupingMode { // TODO: Localize
    /**
     * Keep it with all the other clips
     */
    NONE(Identifier.of(Autocut.MOD_ID, "none"), Text.of("Main file")),
    /**
     * Put it in a file with the things of its type
     */
    TYPE(Identifier.of(Autocut.MOD_ID, "type"), Text.of("Own type")),
    /**
     * Give it its own file
     */
    INDIVIDUAL(Identifier.of(Autocut.MOD_ID, "individual"), Text.of("Separate"));

    private final Identifier id;
    private final Text name;
    ExportGroupingMode(Identifier id, Text displayName) {
        this.id = id;
        this.name = displayName;
    }

    public static @Nullable ExportGroupingMode fromId(Identifier id) {
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
