package com.skycatdev.autocut.clips;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ReceiveServerMessageClipType extends ClipType { // TODO: regex filter?
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "receive_server_message");
    public static final Codec<ReceiveServerMessageClipType> CODEC = RecordCodecBuilder.create((instance) -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ReceiveServerMessageClipType::new));

    public ReceiveServerMessageClipType(boolean shouldRecord, boolean isActive, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode) {
        super(ID, isActive, shouldRecord, startOffset, endOffset, inverse, exportGroupingMode, false, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    public ReceiveServerMessageClipType() {
        super(ID, false, true, 100, 100, false, ExportGroupingMode.NONE, false, true, 100, 100, false, ExportGroupingMode.NONE);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.receive_server_message_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.receive_server_message_clip");
    }

    public Clip createClip(long time, Text message, boolean actionBar) { // TODO: Show what server you are connected to, make a place for actionBar to go
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), isInverse(), "Received a chat message from the server" + (actionBar ? " in action bar" : ""), "server", message.getString(), null, null);
    }

}
