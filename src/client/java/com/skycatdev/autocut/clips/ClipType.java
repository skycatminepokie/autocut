package com.skycatdev.autocut.clips;

import com.google.gson.JsonObject;
import com.skycatdev.autocut.Autocut;
import com.skycatdev.autocut.datagen.AutocutEnglishLangProvider;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.controller.LongFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * A type of {@link Clip} to be recorded.
 * Every implementation of this should have a way to create a clip based on related data.
 */
public abstract class ClipType {
    public static final Identifier START_OFFSET = Identifier.of(Autocut.MOD_ID, "start_offset");
    public static final Identifier END_OFFSET = Identifier.of(Autocut.MOD_ID, "end_offset");
    public static final Identifier SHOULD_RECORD = Identifier.of(Autocut.MOD_ID, "should_record");
    public static final Identifier ACTIVE = Identifier.of(Autocut.MOD_ID, "active");
    /**
     * The {@link Identifier} stored with clips of this type in the database.
     */
    public final Identifier id;
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

    public ClipType(Identifier id, boolean activeDefault, boolean shouldRecordDefault, long startOffsetDefault, long endOffsetDefault, @Nullable JsonObject json) {
        this.id = id;
        this.activeDefault = activeDefault;
        this.shouldRecordDefault = shouldRecordDefault;
        this.startOffsetDefault = startOffsetDefault;
        this.endOffsetDefault = endOffsetDefault;
        deserialize(json);
    }

    /**
     * Deserializes the type from json. Make sure to override this when adding your own config options:
     * <pre>
     *     {@code
     *     super.deserialize(json);
     *     if (json == null) {
     *         this.myOption = this.myOptionDefault;
     *     } else {
     *         this.myOption = json.getAsJsonPrimitive(KEY).getAsBoolean();
     *     }
     *     }
     * </pre>
     * @param json The {@link JsonObject} representing this type.
     */
    public void deserialize(@Nullable JsonObject json) {
        if (json == null) {
            this.active = activeDefault;
            this.shouldRecord = shouldRecordDefault;
            this.startOffset = startOffsetDefault;
            this.endOffset = endOffsetDefault;
        } else {
            this.active = json.getAsJsonPrimitive(ACTIVE.toString()).getAsBoolean();
            this.shouldRecord = json.getAsJsonPrimitive(SHOULD_RECORD.toString()).getAsBoolean();
            this.startOffset = json.getAsJsonPrimitive(START_OFFSET.toString()).getAsLong();
            this.endOffset = json.getAsJsonPrimitive(END_OFFSET.toString()).getAsLong();
        }
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

    /**
     * Creates the YACL options for this type. Override this if you want to add your own options.
     * <pre>
     *     {@code
     *     super.buildOptions();
     *     builder.option(myOption);
     *     builder.option(myOtherOption);
     *     }
     * </pre>
     */
    public void addOptions(OptionGroup.Builder builder) {
        builder.option(Option.<Boolean>createBuilder()
                .name(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.should_record"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.should_record.description")))
                .binding(shouldRecordDefault, this::shouldRecord, this::setShouldRecord)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Boolean>createBuilder()
                .name(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.active"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.active.description")))
                .binding(activeDefault, this::isActive, this::setActive)
                .controller(TickBoxControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Long>createBuilder()
                .name(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.start_offset"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.start_offset.description")))
                .binding(startOffsetDefault, this::getStartOffset, this::setStartOffset)
                .controller(LongFieldControllerBuilder::create)
                .build()
        );
        builder.option(Option.<Long>createBuilder()
                .name(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.end_offset"))
                .description(OptionDescription.of(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".generic.end_offset.description")))
                .binding(endOffsetDefault, this::getEndOffset, this::setEndOffset)
                .controller(LongFieldControllerBuilder::create)
                .build()
        );
    }

    public long getEndOffset() {
        return endOffset;
    }

    public void setEndOffset(long endOffset) {
        this.endOffset = endOffset;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(long startOffset) {
        this.startOffset = startOffset;
    }

    public Identifier getId() {
        return id;
    }

    public abstract OptionDescription getOptionGroupDescription();

    public abstract Text getOptionGroupName();

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Serializes the configuration for this type. Override this if you want to add your own options.
     * <pre>
     *     {@code
     *     var json = super.serialize();
     *     json.addProperty(Identifier.of(MyMod.MOD_ID, "my_option"), getMyOption());
     *     return json;
     *     }
     * </pre>
     */
    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty(SHOULD_RECORD.toString(), shouldRecord());
        json.addProperty(ACTIVE.toString(), isActive());
        json.addProperty(START_OFFSET.toString(), getStartOffset());
        json.addProperty(END_OFFSET.toString(), getEndOffset());
        return json;
    }

    public void setShouldRecord(boolean shouldRecord) {
        this.shouldRecord = shouldRecord;
    }

    public boolean shouldRecord() {
        return shouldRecord;
    }
}
