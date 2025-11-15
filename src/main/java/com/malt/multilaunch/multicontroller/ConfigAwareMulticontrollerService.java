package com.malt.multilaunch.multicontroller;

import com.malt.multilaunch.model.Config;
import java.util.List;

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
    public void sendAssignRequestsToController(List<WindowAssignRequest> requests) {
        if (config.enableMultiControllerIntegration()) {
            delegate.sendAssignRequestsToController(requests);
        }
    }

    @Override
    public void setMode(ControllerMode mode, String substate) {
        if (config.enableMultiControllerIntegration()) {
            delegate.setMode(mode, substate);
        }
    }

    @Override
    public void swapHandles(long hwnd1, long hwnd2) {
        if (config.enableMultiControllerIntegration() && config.swapMultiControllerAssignmentsOnWindowSwap()) {
            delegate.swapHandles(hwnd1, hwnd2);
        }
    }
}
