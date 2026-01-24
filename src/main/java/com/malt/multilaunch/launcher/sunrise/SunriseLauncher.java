package com.malt.multilaunch.launcher.sunrise;

import static java.util.Collections.synchronizedSet;

import com.malt.multilaunch.jna.CoreAssigner;
import com.malt.multilaunch.jna.ProcessAffinityUtils;
import com.malt.multilaunch.jna.ProcessVolumeMuter;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import com.malt.multilaunch.window.WindowUtils;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SunriseLauncher extends Launcher<SunriseApiResponse> {
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

                    awaitReady(processes);
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

    private void awaitReady(List<Process> processes) {
        var doneProcesses = synchronizedSet(new HashSet<Process>());

        for (var process : processes) {
            int coreIndex = coreAssigner.getNextAvailableCore(process.pid());
            new Thread(
                            () -> {
                                try {
                                    LOG.debug("Waiting for process window to be ready: {}", process.pid());
                                    while (!WindowUtils.isWindowReady(process.pid())) {
                                        Thread.sleep(10);
                                    }
                                    LOG.debug("Setting affinity for process {}", process.pid());
                                    Thread.sleep(2000);
                                    ProcessAffinityUtils.setAffinity(process.pid(), coreIndex);
                                    doneProcesses.add(process);
                                } catch (InterruptedException ignored) {
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
