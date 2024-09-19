package com.skycatdev.autocut;

import com.skycatdev.autocut.clips.ClipTypes;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import net.minecraft.text.Text;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        ConfigCategory.Builder clipsCategory = ConfigCategory.createBuilder()
                .name(Text.translatable("autocut.yacl.category.clips"))
                .tooltip(Text.translatable("autocut.yacl.category.clips.tooltip"));
        ClipTypes.CLIP_TYPE_REGISTRY.forEach((clipType) -> clipsCategory.group(clipType.clipType().buildOptionGroup()));
        return parent -> YetAnotherConfigLib.createBuilder()
                .title(Text.translatable("autocut.yacl.title"))
                .category(clipsCategory.build())
                .save(ClipTypes::saveAllClipTypes)
                .build().generateScreen(parent);
    }
}
