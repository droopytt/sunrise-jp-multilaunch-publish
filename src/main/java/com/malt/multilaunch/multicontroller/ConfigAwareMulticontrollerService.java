package com.malt.multilaunch.multicontroller;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import java.util.HashMap;
import java.util.Optional;

public class ConfigAwareMulticontrollerService implements MultiControllerService {

    private final Config config;
    private final MultiControllerService delegate;

    public ConfigAwareMulticontrollerService(Config config, MultiControllerService delegate) {
        this.config = config;
        this.delegate = delegate;
    }

    @Override
    public void unassignAllToons() {
        if (config.enableMultiControllerIntegration()) {
            delegate.unassignAllToons();
        }
    }

    @Override
    public void sendAssignRequestsToController(HashMap<Account, WindowAssignRequest> requests) {
        if (config.enableMultiControllerIntegration()) {
            delegate.sendAssignRequestsToController(requests);
        }
    }

    @Override
    public Optional<WindowAssignRequest> lastAssignedForAccount(Account account) {
        if (config.enableMultiControllerIntegration()) {
            return delegate.lastAssignedForAccount(account);
        }
        return Optional.empty();
    }

    @Override
    public void setMode(ControllerMode mode, String substate) {
        if (config.enableMultiControllerIntegration()) {
            delegate.setMode(mode, substate);
        }
    }

    @Override
    public void swapHandles(Account acct1, long hwnd1, Account acct2, long hwnd2) {
        if (config.enableMultiControllerIntegration() && config.swapMultiControllerAssignmentsOnWindowSwap()) {
            delegate.swapHandles(acct1, hwnd1, acct2, hwnd2);
        }
    }
}
