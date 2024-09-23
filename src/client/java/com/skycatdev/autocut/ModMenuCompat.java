package com.skycatdev.autocut;

import com.skycatdev.autocut.config.ConfigHandler;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuCompat implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ConfigHandler.getConfigScreenFactory();
    }

}
