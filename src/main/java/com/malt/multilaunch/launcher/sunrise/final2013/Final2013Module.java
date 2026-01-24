package com.malt.multilaunch.launcher.sunrise.final2013;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.launcher.sunrise.SunriseLauncherModule;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;

public class Final2013Module extends SunriseLauncherModule {

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new Final2013Launcher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }
}
