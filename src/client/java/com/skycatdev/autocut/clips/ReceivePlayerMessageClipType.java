package com.skycatdev.autocut.clips;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ReceivePlayerMessageClipType extends ClipType { // TODO: regex filter?
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "receive_player_message");
    public static final Codec<ReceivePlayerMessageClipType> CODEC = RecordCodecBuilder.create((instance) -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ReceivePlayerMessageClipType::new));

    public ReceivePlayerMessageClipType(boolean shouldRecord, boolean isActive, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode) {
        super(ID, isActive, shouldRecord, startOffset, endOffset, inverse, exportGroupingMode, false, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    public ReceivePlayerMessageClipType() {
        this(false, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.receive_player_message_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.receive_player_message_clip");
    }

    public Clip createClip(long time, Text message, @Nullable GameProfile sender) { // TODO: Make sure we don't need MessageType.Parameters
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), isInverse(), "Received a chat message from a player", sender == null ? null : sender.getName(), message.getString(), null, null);
    }

}
