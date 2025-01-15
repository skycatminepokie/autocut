package com.skycatdev.autocut.database;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.config.ExportGroupingMode;
import com.skycatdev.autocut.record.RecordingTrigger;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ClipType {
	public static final Codec<ClipType> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RecordingTrigger.REGISTRY.getCodec().fieldOf("start_trigger").forGetter(ClipType::getStartTrigger),
			RecordingTrigger.REGISTRY.getCodec().optionalFieldOf("end_trigger").forGetter((clipType) -> Optional.ofNullable(clipType.getEndTrigger())),
			Codec.LONG.fieldOf("start_offset").forGetter(ClipType::getStartOffset),
			Codec.LONG.fieldOf("end_offset").forGetter(ClipType::getEndOffset),
			Codec.BOOL.fieldOf("enabled").forGetter(ClipType::isEnabled),
			Codec.BOOL.fieldOf("inverted").forGetter(ClipType::isInverted),
			Identifier.CODEC.comapFlatMap((id) -> {
				@Nullable ExportGroupingMode groupingMode = ExportGroupingMode.fromId(id);
				return groupingMode != null ? DataResult.success(groupingMode) : DataResult.error(() -> "Not a valid ExportGroupingMode: " + id);
			}, ExportGroupingMode::getId).fieldOf("group_mode").forGetter(ClipType::getExportGroupingMode)
	).apply(instance, ClipType::new));

	protected RecordingTrigger startTrigger;
	protected @Nullable RecordingTrigger endTrigger;
	protected long startOffset;
	protected long endOffset;
	protected boolean enabled;
	protected boolean inverted;
	protected ExportGroupingMode exportGroupingMode;

	public ClipType(RecordingTrigger startTrigger, @Nullable RecordingTrigger endTrigger, long startOffset, long endOffset, boolean enabled, boolean inverted, ExportGroupingMode exportGroupingMode) {
		this.startTrigger = startTrigger;
		this.endTrigger = endTrigger;
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.enabled = enabled;
		this.inverted = inverted;
		this.exportGroupingMode = exportGroupingMode;
	}

	public ClipType(RecordingTrigger startTrigger,
					@SuppressWarnings("OptionalUsedAsFieldOrParameterType") Optional<RecordingTrigger> endTrigger,
					long startOffset,
					long endOffset,
					boolean enabled,
					boolean inverted,
					ExportGroupingMode exportGroupingMode) {
		this.startTrigger = startTrigger;
		this.endTrigger = endTrigger.orElse(null);
		this.startOffset = startOffset;
		this.endOffset = endOffset;
		this.enabled = enabled;
		this.inverted = inverted;
		this.exportGroupingMode = exportGroupingMode;
	}

	public long getEndOffset() {
		return endOffset;
	}

	public @Nullable RecordingTrigger getEndTrigger() {
		return endTrigger;
	}

	public ExportGroupingMode getExportGroupingMode() {
		return exportGroupingMode;
	}

	public long getStartOffset() {
		return startOffset;
	}

	public RecordingTrigger getStartTrigger() {
		return startTrigger;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isInverted() {
		return inverted;
	}
}
