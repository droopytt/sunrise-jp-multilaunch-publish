package com.malt.multilaunch.launcher;

import com.malt.multilaunch.login.APIResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Launcher<T extends APIResponse> {
    protected final MultiControllerService multiControllerService;
    protected final WindowService windowService;
    private final GameLoginClient<T> gameLoginClient;
    private final Config config;

    public Launcher(
            Config config,
            MultiControllerService multiControllerService,
            WindowService windowService,
            GameLoginClient<T> gameLoginClient) {
        this.config = config;
        this.multiControllerService = multiControllerService;
        this.windowService = windowService;
        this.gameLoginClient = gameLoginClient;
    }

    public Process startGame(Path workingDir, Map<String, String> environmentVars) throws IOException {
        var exePath = workingDir.resolve(executableName()).toAbsolutePath().toString();
        var args = Stream.concat(Stream.of(exePath), processArgs().stream()).toList();
        var processBuilder = new ProcessBuilder(args).directory(workingDir.toFile());
        var allEnvironmentVars = processBuilder.environment();
        allEnvironmentVars.putAll(environmentVars);
        return processBuilder.start();
    }

    public Process launch(Account account, Path workingDir) {
        try {
            var response = gameLoginClient.login(account.username(), account.password(), additionalLoginArgs());
            var environmentVars = getEnvironmentVariables(response);
            return startGame(workingDir, environmentVars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Map<String, String> additionalLoginArgs();

    public abstract void performPostLoginOverrides(
            List<Account> accounts, ActiveAccountManager activeAccountManager, Config config);

    public abstract Map<String, String> getEnvironmentVariables(T response);

    public abstract List<String> processArgs();

    public abstract String executableName();

    public Path workingDir() {
        return config.jpWorkingDir();
    }

    public abstract void onProcessEnd(Process process);

    public abstract String canonicalName();
}
