package com.skycatdev.autocut.clips;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import dev.isxander.yacl3.api.OptionDescription;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ReceiveServerMessageClipType extends ClipType { // TODO: regex filter?
    public static final Identifier ID = Identifier.of(Autocut.MOD_ID, "receive_server_message");
    public static final Codec<ReceiveServerMessageClipType> CODEC = RecordCodecBuilder.create((instance) -> ClipTypes.addDefaultConfigFields(instance).apply(instance, ReceiveServerMessageClipType::new));

    public ReceiveServerMessageClipType(boolean shouldRecord, boolean isActive, long startOffset, long endOffset) {
        super(ID, isActive, shouldRecord, startOffset, endOffset, false, true, 100, 100);
    }

    public ReceiveServerMessageClipType() {
        super(ID, false, true, 100, 100, false, true, 100, 100);
    }

    @Override
    public OptionDescription getOptionGroupDescription() {
        return OptionDescription.of(Text.translatable("autocut.yacl.receive_server_message_clip.description"));
    }

    @Override
    public Text getOptionGroupName() {
        return Text.translatable("autocut.yacl.receive_server_message_clip");
    }

    public Clip createClip(long time, Text message, boolean overlay) { // TODO: Show what server you are connected to
        return new Clip(time - getStartOffset(), time, time + getEndOffset(), ID, isActive(), "Received a chat message from the server", "server", message.getString(), null, null);
    }

}