package com.skycatdev.autocut.database;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.config.ExportGroupingMode;
import com.skycatdev.autocut.trigger.RecordingTrigger;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Optional;

public class ClipType {
	public static final Codec<ClipType> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			RecordingTrigger.CODEC.fieldOf("start_trigger").forGetter(ClipType::getStartTrigger),
			RecordingTrigger.CODEC.optionalFieldOf("end_trigger").forGetter((clipType) -> Optional.ofNullable(clipType.getEndTrigger())),
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

	private ClipType(RecordingTrigger startTrigger,
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

	/**
	 * Load a ClipType from the given file.
	 * @param file The file to read from.
	 * @return THe read ClipType, or {@code null} if the read failed.
	 */
	public static @Nullable ClipType load(File file) throws FileNotFoundException {
		JsonElement element = JsonParser.parseReader(new FileReader(file));
		var data = CODEC.decode(JsonOps.INSTANCE, element);
		if (data.isSuccess()) {
			return data.getOrThrow().getFirst();
		}
		Autocut.LOGGER.warn("Failed to load ClipType from {}", file.getName());
		return null;
	}
}
