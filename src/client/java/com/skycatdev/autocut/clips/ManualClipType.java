package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.concurrent.TimeUnit;

import static com.skycatdev.autocut.AutocutClient.CLIP_KEYBIND;

/**
 * A clip created by pressing the clip keybind.
 */
public class ManualClipType extends ClipType {
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "manual_clip");
    public static final Codec<ManualClipType> CODEC = RecordCodecBuilder.create(instance -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ManualClipType::new));

    public ManualClipType(boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse) {
        super(ID, active, shouldRecord, startOffset, endOffset, inverse, true, true, TimeUnit.SECONDS.toMicros(30), TimeUnit.SECONDS.toMicros(3), false);
    }

    public ManualClipType() {
        super(ID, true, true, TimeUnit.SECONDS.toMicros(30), TimeUnit.SECONDS.toMicros(3), false, true, true,TimeUnit.SECONDS.toMicros(30), TimeUnit.SECONDS.toMicros(3), false);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.manual_clip.description", Text.translatable(CLIP_KEYBIND.getDefaultKey().getTranslationKey()), Text.keybind(CLIP_KEYBIND.getTranslationKey())));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.manual_clip");
    }

    public Clip createClip(long time) {
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Manual", null, null, null, null);
    }
}
