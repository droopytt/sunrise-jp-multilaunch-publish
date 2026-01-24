package com.malt.multilaunch.launcher.launchers;

import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.servers.Server;
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
        return jpExecutableName();
    }

    @Override
    public Path workingDir() {
        return config.jpWorkingDir();
    }

    public static String jpExecutableName() {
        return "py24.exe";
    }

    @Override
    protected Map<String, String> additionalLoginArgs() {
        return Map.of("serverType", "Toontown Japan 2010");
    }

    @Override
    public void onProcessEnd(Process process) {
        coreAssigner.removeAssignedCore(process.pid());
    }

    @Override
    public String canonicalName() {
        return Server.SUNRISE_JP.canonicalName();
    }
}
