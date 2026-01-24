package com.malt.multilaunch.launcher.sunrise.test2004;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.jna.CoreAssigner;
import com.malt.multilaunch.launcher.*;
import com.malt.multilaunch.launcher.sunrise.SunriseLauncherModule;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;

public class Test2004Module extends SunriseLauncherModule {

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new Test2004Launcher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }
}
