package com.skycatdev.autocut.datagen;

import com.skycatdev.autocut.Autocut;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class AutocutEnglishLangProvider extends FabricLanguageProvider {
    protected static final String YACL_PREFIX = "configurable." + Autocut.MOD_ID + ".yacl";

    protected AutocutEnglishLangProvider(FabricDataOutput dataGenerator, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataGenerator, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder tb) {
        tb.add(YACL_PREFIX + ".category.clips", "Clips");

        addOptionGroup("place_block_clip", "Place block", "When you place a block", tb);
        addOptionGroup("shoot_player_clip", "Shoot player", "When you shoot a player. Note that some servers will \"fake\" this in order to play the sound that comes with it. On Hypixel, you'll notice this when you join a game. You can filter these out if you like - it seems to only happen when you're at x=8.5, z=8.5.", tb);

        addOption("default_start_offset", "Default start offset", "How many milliseconds before the event should be counted as part of the clip.", tb);
        addOption("default_end_offset", "Default end offset", "How many milliseconds after the event should be counted as part of the clip.", tb);
        addOption("should_record", "Enable recording", "Whether this event should be recorded.", tb);
    }

    private void addOption(String key, String optionName, String description, TranslationBuilder tb) {
        tb.add(YACL_PREFIX + ".option." + key, optionName);
        tb.add(YACL_PREFIX + ".description." + key, description);
    }

    private void addOptionGroup(String key, String groupName, String description, TranslationBuilder tb) {
        tb.add(YACL_PREFIX + ".option_group." + key, groupName);
        tb.add(YACL_PREFIX + ".description." + key, description);
    }
}
