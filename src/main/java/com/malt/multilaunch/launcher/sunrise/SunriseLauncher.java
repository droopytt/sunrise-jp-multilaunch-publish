package com.malt.multilaunch.launcher.sunrise;

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
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SunriseLauncher extends Launcher<SunriseApiResponse> {
    public static final String AVATAR_CHOOSER_LINE = "StateData: AvatarChooser.enter";
    private static final Logger LOG = LoggerFactory.getLogger(SunriseLauncher.class);
    protected final CoreAssigner coreAssigner;

    public SunriseLauncher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        super(config, multiControllerService, windowService, gameLoginClient);
        this.coreAssigner = coreAssigner;
    }

    @Override
    public void performPostLoginOverrides(
            List<Account> accounts, ActiveAccountManager activeAccountManager, Config config) {
        CompletableFuture.supplyAsync(() -> {
                    var processes = accounts.stream()
                            .map(activeAccountManager::findProcessForAccount)
                            .flatMap(Optional::stream)
                            .toList();

                    waitForLine(processes, targetAvatarLine());
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

    protected String targetAvatarLine() {
        return "Using gameServer from launcher";
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

                                    LOG.trace("Process is alive: {} {}", process.pid(), process.isAlive());
                                    while (process.isAlive()) {
                                        LOG.trace("Stdout reader ready: {}", stdoutReader.ready());
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
    public void onProcessEnd(Process process) {
        coreAssigner.removeAssignedCore(process.pid());
    }
}
