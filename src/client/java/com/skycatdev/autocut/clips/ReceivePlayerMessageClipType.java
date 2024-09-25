package com.skycatdev.autocut.clips;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ReceivePlayerMessageClipType extends ClipType { // TODO: regex filter?
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "receive_player_message");
    public static final Codec<ReceivePlayerMessageClipType> CODEC = RecordCodecBuilder.create((instance) -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ReceivePlayerMessageClipType::new));

    public ReceivePlayerMessageClipType(boolean shouldRecord, boolean isActive, long startOffset, long endOffset) {
        super(ID, isActive, shouldRecord, startOffset, endOffset, false, true, 100, 100);
    }

    public ReceivePlayerMessageClipType() {
        super(ID, false, true, 100, 100, false, true, 100, 100);
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
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Received a chat message from a player", sender == null ? null : sender.getName(), message.getString(), null, null);
    }

}