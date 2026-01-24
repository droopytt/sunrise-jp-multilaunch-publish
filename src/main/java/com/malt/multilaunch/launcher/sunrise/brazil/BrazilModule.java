package com.malt.multilaunch.launcher.sunrise.brazil;

import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.jna.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.launcher.sunrise.SunriseLauncherModule;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;

public class BrazilModule extends SunriseLauncherModule {

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new BrazilLauncher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }
}
