package com.malt.multilaunch.hotkeys;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class SnapWindowsAction implements Runnable {

    private final ActiveAccountManager activeAccountManager;
    private final WindowService windowService;
    private final MultiControllerService multiControllerService;
    private final Supplier<List<Account>> accountSupplier;

    public SnapWindowsAction(
            ActiveAccountManager activeAccountManager,
            WindowService windowService,
            MultiControllerService multiControllerService,
            Supplier<List<Account>> accountSupplier) {
        this.activeAccountManager = activeAccountManager;
        this.windowService = windowService;
        this.multiControllerService = multiControllerService;
        this.accountSupplier = accountSupplier;
    }

    @Override
    public void run() {
        var openAccounts = accountSupplier.get();
        windowService.resizeWindowsWithRectangles(
                openAccounts,
                activeAccountManager,
                activeAccountManager.accounts().stream()
                        .sorted(Comparator.comparingInt(openAccounts::indexOf))
                        .map(activeAccountManager::findWindowRect)
                        .flatMap(Optional::stream)
                        .toList());
    }
}
