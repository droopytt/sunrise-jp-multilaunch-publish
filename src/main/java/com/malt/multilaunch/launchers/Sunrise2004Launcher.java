package com.malt.multilaunch.launchers;

import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.util.List;
import java.util.Map;

public class Sunrise2004Launcher extends Launcher<Sunrise2004ApiResponse> {
    public Sunrise2004Launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<Sunrise2004ApiResponse> gameLoginClient) {
        super(config, multiControllerService, windowService, gameLoginClient);
    }

    @Override
    protected Map<String, String> additionalLoginArgs() {
        return Map.of();
    }

    @Override
    public void performPostLoginOverrides(
            List<Account> accounts, ActiveAccountManager activeAccountManager, Config config) {}

    @Override
    public Map<String, String> getEnvironmentVariables(Sunrise2004ApiResponse response) {
        return Map.of();
    }

    @Override
    public List<String> processArgs() {
        return List.of();
    }

    @Override
    public String executableName() {
        return "";
    }

    @Override
    public void onProcessEnd(Process process) {}

    @Override
    public String canonicalName() {
        return "";
    }
}
