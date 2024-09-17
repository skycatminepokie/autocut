package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.ClipTypes;
import com.skycatdev.autocut.datagen.AutocutEnglishLangProvider;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.text.Text;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ConfigCategory.Builder clipsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".category.clips"))
                .tooltip(Text.translatable(AutocutEnglishLangProvider.YACL_PREFIX + ".category.clips.tooltip"));
        ClipTypes.TYPE_REGISTRY.forEach((clipType) -> clipsCategory.group(clipType.buildOptionGroup()));
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Text.of("Autocut configuration menu"))
                .category(clipsCategory.build())
                .build().generateScreen(parent);
    }
}
