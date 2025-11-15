package com.malt.multilaunch.multicontroller;

import com.malt.multilaunch.model.Config;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface MultiControllerService {
    Logger LOG = LoggerFactory.getLogger(MultiControllerService.class);

    void unassignAllToons();

    void sendAssignRequestsToController(List<WindowAssignRequest> requests);

    void setMode(ControllerMode mode, String substate);

    static MultiControllerService createDefault(Config config) {
        LOG.debug("Creating multicontroller service, enabled: {}", config.enableMultiControllerIntegration());
        return new ConfigAwareMulticontrollerService(config, new DefaultMulticontrollerService());
    }

    void swapHandles(long hwnd1, long hwnd2);
}
