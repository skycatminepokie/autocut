package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class NotInWorldClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "not_in_world");
    public static final Codec<NotInWorldClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, NotInWorldClipType::new));
    protected @Nullable ClipBuilder currentClip = null;

    public NotInWorldClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, exportGroupingMode, false, true, 0, 0, true, ExportGroupingMode.NONE);
    }

    public NotInWorldClipType() {
        this(false, true, 0, 0, true, ExportGroupingMode.NONE);
    }

    /**
     * @param time The time that the world was left, usually the current time.
     * @return A clip of the most recent record of not being in a world, or null if {@link NotInWorldClipType#leaveWorld(long)} was not called first.
     */
    public @Nullable Clip enterWorld(long time) {
        if (currentClip != null) {
            currentClip.setOut(time + getEndOffset());
            Clip finishedClip = currentClip.build();
            currentClip = null;
            return finishedClip;
        }
        return null;
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.not_in_world.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.not_in_world");
    }

    public void leaveWorld(long time) {
        if (currentClip == null) {
            currentClip = new ClipBuilder(time - getStartOffset(), time, 0L, getId(), isActive(), isInverse(), getExportGroupingMode()); // "out" param doesn't matter
        }
    }


}
