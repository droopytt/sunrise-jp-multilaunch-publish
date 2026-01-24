package com.malt.multilaunch.launcher.launchers.jp;

import com.google.inject.*;
import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.launcher.launchers.SunriseLauncherModule;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;

public class SunriseJpUltiLauncherModule extends SunriseLauncherModule {

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new JPLauncher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }
}
