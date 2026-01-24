package com.malt.multilaunch.launcher.sunrise.jp;

import com.malt.multilaunch.jna.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Server;
import com.malt.multilaunch.launcher.sunrise.SunriseLauncher;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.window.WindowService;
import java.nio.file.Path;
import java.util.*;

public class JPLauncher extends SunriseLauncher {

    public JPLauncher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        super(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }

    @Override
    public Map<String, String> getEnvironmentVariables(SunriseApiResponse response) {
        var map = new HashMap<String, String>(3);
        map.put("GAME_SERVER", "unite.sunrise.games:6667");
        map.put("DOWNLOAD_SERVER", "http://download.sunrise.games/launcher/");
        map.put("PLAY_TOKEN", response.cookie());
        return map;
    }

    @Override
    public List<String> processArgs() {
        return List.of("launcher.py");
    }

    @Override
    public String executableName() {
        return "py24.exe";
    }

    @Override
    public Path workingDir() {
        return config.jpWorkingDir();
    }

    @Override
    protected Map<String, String> additionalLoginArgs() {
        return Map.of("serverType", "Toontown Japan 2010");
    }

    @Override
    public String canonicalName() {
        return Server.SUNRISE_JP.canonicalName();
    }
}
