package com.malt.multilaunch;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.ui.ConfigService;

public class ConfigModule extends AbstractModule {
    @Provides
    @Singleton
    public ConfigService configService() {
        return ConfigService.create();
    }

    @Provides
    @Singleton
    public Config config(ConfigService configService) {
        return configService.load();
    }
}
