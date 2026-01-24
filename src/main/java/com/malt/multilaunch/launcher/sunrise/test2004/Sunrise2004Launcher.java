package com.malt.multilaunch.launcher.sunrise.test2004;

import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Server;
import com.malt.multilaunch.launcher.sunrise.SunriseLauncher;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;
import java.nio.file.Path;
import java.util.*;

public class Sunrise2004Launcher extends SunriseLauncher {

    public Sunrise2004Launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        super(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }

    @Override
    public Map<String, String> getEnvironmentVariables(SunriseApiResponse response) {
        var map = new HashMap<String, String>(2);
        map.put("DOWNLOAD_SERVER", "http://download.sunrise.games/launcher/");
        map.put("TOONTOWN_PLAYTOKEN", response.cookie());
        return map;
    }

    @Override
    protected Map<String, String> additionalLoginArgs() {
        return Map.of("serverType", "Test Toontown 2004 (sv1.0.10.6.test)");
    }

    @Override
    public List<String> processArgs() {
        return List.of("Start.py", "0", "test.toontown.sv10106.sunrise.games:6667", "unite.sunrise.games:4500", "1");
    }

    @Override
    public String executableName() {
        return "python.exe";
    }

    @Override
    public Path workingDir() {
        return config.sunrise2004WorkingDir();
    }

    @Override
    public String canonicalName() {
        return Server.SUNRISE_2004.canonicalName();
    }
}
