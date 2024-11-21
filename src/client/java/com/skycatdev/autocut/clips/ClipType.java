package com.skycatdev.autocut.clips;

import com.skycatdev.autocut.config.ExportGroupingMode;
import com.skycatdev.autocut.datagen.AutocutEnglishLangProvider;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * A type of {@link Clip} to be recorded.
 * Every implementation of this should have a way to create a clip based on related data.
 */
public abstract class ClipType {
    /**
     * The {@link Identifier} stored with clips of this type in the database.
     */
    public final Identifier id;
    /**
     * Whether new clips of this type should be marked as active.
     */
    private final boolean activeDefault;
    /**
     * Whether new clips of this type should be recorded.
     */
    private final boolean shouldRecordDefault;
    /**
     * How many milliseconds before the actual event should start by default.
     */
    private final long startOffsetDefault;
    /**
     * How many milliseconds after the actual event should start by default.
     */
    private final long endOffsetDefault;
    /**
     * If the clip should be inverse by default.
     */
    private final boolean inverseDefault;
    /**
     * What group a clip should be exported with by default
     *
     * @see ExportGroupingMode
     */
    private final ExportGroupingMode exportGroupingModeDefault;
    /**
     * Whether new clips of this type should be marked as active.
     */
    private boolean active;
    /**
     * Whether new clips of this type should be recorded.
     */
    private boolean shouldRecord;
    /**
     * How many milliseconds before the actual event should start.
     */
    private long startOffset;
    /**
     * How many milliseconds after the actual event should start.
     */
    private long endOffset;
    /**
     * If the clips of this type should be an inverted clip ("discard this footage")
     */
    private boolean inverse;
    /**
     * What group a clip should be exported with
     *
     * @see ExportGroupingMode
     */
    private ExportGroupingMode exportGroupingMode;

    public ClipType(Identifier id, boolean active, boolean shouldRecord, long startOffset, long endOffset, boolean inverse, ExportGroupingMode exportGroupingMode, boolean activeDefault, boolean shouldRecordDefault, long startOffsetDefault, long endOffsetDefault, boolean inverseDefault, ExportGroupingMode exportGroupingModeDefault) {
        this.id = id;

        this.active = active;
        this.shouldRecord = shouldRecord;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.inverse = inverse;
        this.exportGroupingMode = exportGroupingMode;

        this.activeDefault = activeDefault;
        this.shouldRecordDefault = shouldRecordDefault;
        this.startOffsetDefault = startOffsetDefault;
        this.endOffsetDefault = endOffsetDefault;
        this.inverseDefault = inverseDefault;
        this.exportGroupingModeDefault = exportGroupingModeDefault;
    }

    /**
     * Creates the YACL options for this type. Override this if you want to add your own options.
     * <pre>
     *     {@code
     *     super.addOptions();
     *     builder.option(myOption);
     *     builder.option(myOtherOption);
     *     }
     * </pre>
     */
    public void addOptions(OptionGroup.Builder builder) {
        builder.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.should_record"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.should_record.description")))
                .binding(shouldRecordDefault, this::shouldRecord, this::setShouldRecord)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.active"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.active.description")))
                .binding(activeDefault, this::isActive, this::setActive)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Long>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.start_offset"))
                .description(OptionDescription.of(Text.translatable("autocut.yacl.generic.start_offset.description")))
                .binding(startOffsetDefault, this::getStartOffset, this::setStartOffset)
                .controller(LongFieldControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Long>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.end_offset"))
                .description(OptionDescription.of(Text.translatable("autocut.yacl.generic.end_offset.description")))
                .binding(endOffsetDefault, this::getEndOffset, this::setEndOffset)
                .controller(LongFieldControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Boolean>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.inverse"))
                .description(OptionDescription.of(Text.translatable("autocut.yacl.generic.inverse.description")))
                .binding(inverseDefault, this::isInverse, this::setInverse)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );
        builder.option(Option.<ExportGroupingMode>createBuilder()
                .name(Text.translatable("autocut.yacl.generic.grouping_mode"))
                .description(OptionDescription.of(Text.translatable("autocut.yacl.generic.grouping_mode.description")))
                .binding(exportGroupingModeDefault, this::getExportGroupingMode, this::setExportGroupingMode)
                .controller((option) -> EnumControllerBuilder.create(option).enumClass(ExportGroupingMode.class).formatValue(ExportGroupingMode::getName))
                .build()
        );
    }

    /**
     * Builds the YACL option group for this type. You should not need to touch this.
     */
    public OptionGroup buildOptionGroup() {
        OptionGroup.Builder builder = OptionGroup.createBuilder().name(getOptionGroupName())
                .description(getOptionGroupDescription());
        addOptions(builder);
        return builder.build();
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }

    public ExportGroupingMode getExportGroupingMode() {
        return exportGroupingMode;
    }

    public void setExportGroupingMode(ExportGroupingMode exportGroupingMode) {
        this.exportGroupingMode = exportGroupingMode;
    }

    public Identifier getId() {
        return id;
    }

    public abstract OptionDescription getOptionGroupDescription();

    public abstract Text getOptionGroupName();

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInverse() {
        return inverse;
    }

    public void setInverse(boolean inverse) {
        this.inverse = inverse;
    }

    public void setShouldRecord(boolean shouldRecord) {
        this.shouldRecord = shouldRecord;
    }

    public boolean shouldRecord() {
        return shouldRecord;
    }
}
