package com.skycatdev.autocut.clips;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.RecordingHandler;
import net.minecraft.util.Identifier;

import java.util.Collection;

/**
 * Stores {@link Identifier}s that show the reason for a recording element (clip or event).
 */
public class ClipTypes {
    /**
     * An element made using debug methods. Ideally not used by the end user.
     */
    public static final Identifier DEBUG = Identifier.of(Autocut.MOD_ID, "debug");
    /**
     * An element made for internal use. Should not end up being serialized.
     * @see RecordingHandler#mergeClips(Collection)
     */
    public static final Identifier INTERNAL = Identifier.of(Autocut.MOD_ID, "internal");
    /**
     * An element based on a block breaking.
     */
    public static final Identifier BREAK_BLOCK = Identifier.of(Autocut.MOD_ID, "break_block");
    /**
     * An element triggered by a manual input, but which was not manually constructed.
     * An element made via a keybind for clipping probably should have this id, but an element made by choosing start and end points through a GUI should not.
     * @see ClipTypes#MANUAL_MADE
     */
    public static final Identifier MANUAL_TRIGGERED = Identifier.of(Autocut.MOD_ID, "manual");
    /**
     * An element created manually - times selected by hand.
     * @see ClipTypes#MANUAL_TRIGGERED
     */
    public static final Identifier MANUAL_MADE = Identifier.of(Autocut.MOD_ID, "manual_made");
    /**
     * An element created by attacking an entity.
     */
    public static final Identifier ATTACK_ENTITY = Identifier.of(Autocut.MOD_ID, "attack_entity");
}
