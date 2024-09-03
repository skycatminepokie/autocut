package com.skycatdev.autocut.clips;

import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.RecordingManager;
import net.minecraft.util.Identifier;

import java.util.Collection;

/**
 * Stores {@link Identifier}s that show the reason for a clip.
 */
public class ClipTypes {
    /**
     * A clip made using debug methods. Ideally not used by the end user.
     */
    public static final Identifier DEBUG = Identifier.of(Autocut.MOD_ID, "debug");
    /**
     * A clip made for internal use. Should not end up being serialized.
     * @see RecordingManager#mergeClips(Collection)
     */
    public static final Identifier INTERNAL = Identifier.of(Autocut.MOD_ID, "internal");
    /**
     * A clip based on a block breaking.
     */
    public static final Identifier BREAK_BLOCK = Identifier.of(Autocut.MOD_ID, "break_block");
    /**
     * A clip triggered by a manual input, but which was not manually constructed.
     * A clip made via a keybind for clipping probably should have this id, but a clip made by choosing start and end points through a GUI should not.
     * @see ClipTypes#MANUAL_MADE
     */
    public static final Identifier MANUAL_TRIGGERED = Identifier.of(Autocut.MOD_ID, "manual");
    /**
     * An clip created manually - times selected by hand.
     * @see ClipTypes#MANUAL_TRIGGERED
     */
    public static final Identifier MANUAL_MADE = Identifier.of(Autocut.MOD_ID, "manual_made");
    /**
     * A clip created by attempting to attack an entity.
     */
    public static final Identifier ATTACK_ENTITY = Identifier.of(Autocut.MOD_ID, "attack_entity");
    /**
     * A clip created by attempting to use an item.
     */
    public static final Identifier USE_ITEM = Identifier.of(Autocut.MOD_ID, "use_item");
    /**
     * A clip created by successfully placing a block.
     */
    public static final Identifier PLACE_BLOCK = Identifier.of(Autocut.MOD_ID, "place_block");
    /**
     * A clip created by shooting a player with an arrow.
     */
    public static final Identifier SHOOT_PLAYER = Identifier.of(Autocut.MOD_ID, "shoot_player");
}
