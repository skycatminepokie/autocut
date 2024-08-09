package com.skycatdev.autocut;

import net.minecraft.util.Identifier;

import java.util.Collection;

public class ClipTypes {
    /**
     * A clip made using debug methods. Ideally not used by the end user.
     */
    public static final Identifier DEBUG = Identifier.of(Autocut.MOD_ID, "debug");
    /**
     * A clip made for internal use. Should not end up being serialized.
     * @see Recorder#mergeClips(Collection)
     */
    public static final Identifier INTERNAL = Identifier.of(Autocut.MOD_ID, "internal");
    /**
     * A clip made because the player broke a block.
     */
    public static final Identifier BREAK_BLOCK = Identifier.of(Autocut.MOD_ID, "break_block");
}
