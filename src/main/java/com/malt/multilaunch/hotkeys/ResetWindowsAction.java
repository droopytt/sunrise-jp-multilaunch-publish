package com.malt.multilaunch.hotkeys;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ResetWindowsAction implements Runnable {

    private final Config config;
    private final ActiveAccountManager activeAccountManager;
    private final WindowService windowService;
    private final MultiControllerService multiControllerService;
    private final Supplier<List<Account>> accountSupplier;

    public ResetWindowsAction(
            Config config,
            ActiveAccountManager activeAccountManager,
            WindowService windowService,
            MultiControllerService multiControllerService,
            Supplier<List<Account>> accountSupplier) {
        this.config = config;
        this.activeAccountManager = activeAccountManager;
        this.windowService = windowService;
        this.multiControllerService = multiControllerService;
        this.accountSupplier = accountSupplier;
    }

    @Override
    public void run() {
        var openAccounts = accountSupplier.get();

        var processes = accountSupplier.get().stream()
                .map(activeAccountManager::findProcessForAccount)
                .flatMap(Optional::stream)
                .toList();
        windowService.resizeWindowsForAccounts(openAccounts, activeAccountManager, config);
        reassignControllersForProcesses(processes);
    }

    private void reassignControllersForProcesses(List<Process> processes) {
        try (var executor = Executors.newSingleThreadScheduledExecutor()) {
            multiControllerService.unassignAllToons();
            executor.schedule(
                    () -> windowService.assignControllerToWindows(
                            false, multiControllerService, activeAccountManager, processes),
                    100,
                    TimeUnit.MILLISECONDS);
        }
    }
}
