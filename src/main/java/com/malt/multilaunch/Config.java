package com.malt.multilaunch;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private boolean enableMultiControllerIntegration;
    private boolean swapMultiControllerAssignmentsOnWindowSwap;

    public Config(
            @JsonProperty("enableMultiControllerIntegration") boolean enableMultiControllerIntegration,
            @JsonProperty("swapMultiControllerAssignmentsOnWindowSwap")
                    boolean swapMultiControllerAssignmentsOnWindowSwap) {
        this.enableMultiControllerIntegration = enableMultiControllerIntegration;
        this.swapMultiControllerAssignmentsOnWindowSwap = swapMultiControllerAssignmentsOnWindowSwap;
    }

    @JsonGetter
    public boolean enableMultiControllerIntegration() {
        return enableMultiControllerIntegration;
    }

    @JsonGetter
    public boolean swapMultiControllerAssignmentsOnWindowSwap() {
        return swapMultiControllerAssignmentsOnWindowSwap;
    }

    @JsonSetter
    public void setEnableMultiControllerIntegration(boolean enableMultiControllerIntegration) {
        this.enableMultiControllerIntegration = enableMultiControllerIntegration;
    }

    @JsonSetter
    public void setSwapMultiControllerAssignmentsOnWindowSwap(boolean multiControllerSwaps) {
        this.swapMultiControllerAssignmentsOnWindowSwap = multiControllerSwaps;
    }
}
