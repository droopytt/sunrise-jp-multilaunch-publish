package com.malt.multilaunch.launcher.launchers;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.*;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;

public class Sunrise2004UltiLauncherModule extends SunriseLauncherModule {

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new Sunrise2004Launcher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }
}
