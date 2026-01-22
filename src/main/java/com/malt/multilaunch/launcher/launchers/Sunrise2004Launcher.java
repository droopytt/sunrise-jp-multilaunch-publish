package com.malt.multilaunch.launcher.launchers;

import static java.util.Collections.synchronizedSet;

import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.ffm.ProcessAffinityUtils;
import com.malt.multilaunch.ffm.ProcessVolumeMuter;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.servers.Server;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sunrise2004Launcher extends Launcher<SunriseApiResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(Sunrise2004Launcher.class);
    private final CoreAssigner coreAssigner;

    public Sunrise2004Launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        super(config, multiControllerService, windowService, gameLoginClient);
        this.coreAssigner = coreAssigner;
    }

    @Override
    public Map<String, String> getEnvironmentVariables(SunriseApiResponse response) {
        var map = new HashMap<String, String>(2);
        map.put("DOWNLOAD_SERVER", "http://download.sunrise.games/launcher/");
        map.put("TOONTOWN_PLAYTOKEN", response.cookie());
        map.put("GAME2_DONE", "1");
        return map;
    }

    @Override
    public void performPostLoginOverrides(
            List<Account> accounts, ActiveAccountManager activeAccountManager, Config config) {
        CompletableFuture.supplyAsync(() -> {
                    var processes = accounts.stream()
                            .map(activeAccountManager::findProcessForAccount)
                            .flatMap(Optional::stream)
                            .toList();

                    waitForLine(processes, "Using gameServer from launcher:");
                    return processes;
                })
                .thenAccept(processes -> {
                    CompletableFuture.runAsync(() -> windowService.resizeWindowsForAccounts(
                            accounts, activeAccountManager, config.stickySessions()));

                    CompletableFuture.runAsync(() -> windowService.assignControllerToWindows(
                            config.stickySessions(), multiControllerService, activeAccountManager, processes));

                    CompletableFuture.runAsync(() -> setVolumeForAccounts(activeAccountManager, config));
                });
    }

    private void setVolumeForAccounts(ActiveAccountManager activeAccountManager, Config config) {
        activeAccountManager.activeAccounts().stream()
                .filter(account -> !account.audio())
                .peek(account -> LOG.debug("Muting process for account {}", account))
                .map(activeAccountManager::findProcessForAccount)
                .flatMap(Optional::stream)
                .forEach(process -> ProcessVolumeMuter.muteProcess(process.pid()));

        activeAccountManager.activeAccounts().stream()
                .filter(Account::audio)
                .peek(account -> LOG.debug("Enabling audio for account {}", account))
                .map(activeAccountManager::findProcessForAccount)
                .flatMap(Optional::stream)
                .forEach(process ->
                        ProcessVolumeMuter.setProcessVolume(process.pid(), config.volumePercentage() / 100f, false));
    }

    @Override
    public List<String> processArgs() {
        return List.of("Start.py", "0", "test.toontown.sv10106.sunrise.games:6667", "unite.sunrise.games:4500", "1");
    }

    @Override
    public String executableName() {
        return sunrise2004ExecutableName();
    }

    @Override
    public Path workingDir() {
        return config.sunrise2004WorkingDir();
    }

    public static String sunrise2004ExecutableName() {
        return "python.exe";
    }

    private void waitForLine(List<Process> processes, String targetLine) {
        var doneProcesses = synchronizedSet(new HashSet<Process>());

        for (var process : processes) {
            int coreIndex = coreAssigner.getNextAvailableCore(process.pid());
            var waitUntil = Instant.now().plusSeconds(90);
            new Thread(
                            () -> {
                                try {
                                    var stdoutReader =
                                            new BufferedReader(new InputStreamReader(process.getInputStream()));
                                    var stderrReader =
                                            new BufferedReader(new InputStreamReader(process.getErrorStream()));
                                    boolean affinitySet = false;

                                    while (process.isAlive()) {
                                        while (stdoutReader.ready()) {
                                            var line = stdoutReader.readLine();
                                            if (line == null) {
                                                break;
                                            }

                                            if (!affinitySet && Instant.now().isBefore(waitUntil)) {
                                                LOG.trace("PID: {}: {}", process.pid(), line);
                                            }

                                            if (!affinitySet && line.contains(targetLine)) {
                                                ProcessAffinityUtils.setAffinity(process.pid(), coreIndex);
                                                doneProcesses.add(process);
                                                affinitySet = true;
                                            }
                                        }

                                        while (stderrReader.ready()) {
                                            stderrReader.readLine();
                                        }

                                        Thread.sleep(10);
                                    }

                                    while (stdoutReader.readLine() != null) {}
                                    while (stderrReader.readLine() != null) {}

                                } catch (IOException | InterruptedException ignored) {
                                }
                            },
                            "Awaiter-Thread-%d".formatted(process.pid()))
                    .start();
        }

        var waitUntil = Instant.now().plusSeconds(90);
        while (doneProcesses.size() < processes.size() && Instant.now().isBefore(waitUntil)) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        var endTime = Instant.now();
        if (endTime.isBefore(waitUntil)) {
            LOG.info(
                    "Managed to assign all cores before timer expires at {} ({} seconds to spare)",
                    waitUntil,
                    ((waitUntil.toEpochMilli() - endTime.toEpochMilli()) / 1000));
        } else {
            LOG.info("Did not manage to assign cores before timer expires");
        }

        LOG.info("All processes ready or timeout reached, returning from method.");
    }

    @Override
    protected Map<String, String> additionalLoginArgs() {
        return Map.of("serverType", "Test Toontown 2004 (sv1.0.10.6.test)");
    }

    @Override
    public void onProcessEnd(Process process) {
        coreAssigner.removeAssignedCore(process.pid());
    }

    @Override
    public String canonicalName() {
        return Server.SUNRISE_2004.canonicalName();
    }
}
