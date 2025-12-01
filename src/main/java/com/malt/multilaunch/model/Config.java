package com.malt.multilaunch.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.nio.file.Path;
import java.nio.file.Paths;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private boolean enableMultiControllerIntegration;
    private boolean swapMultiControllerAssignmentsOnWindowSwap;
    private int startingCore;
    private boolean stickySessions;
    private Path sunriseWorkingDir;

    public Config(
            @JsonProperty("enableMultiControllerIntegration") boolean enableMultiControllerIntegration,
            @JsonProperty("swapMultiControllerAssignmentsOnWindowSwap")
                    boolean swapMultiControllerAssignmentsOnWindowSwap,
            @JsonProperty("startingCore") int startingCore,
            @JsonProperty("stickySessions") boolean stickySessions,
            @JsonProperty("sunriseWorkingDir") Path sunriseWorkingDir) {
        this.enableMultiControllerIntegration = enableMultiControllerIntegration;
        this.swapMultiControllerAssignmentsOnWindowSwap = swapMultiControllerAssignmentsOnWindowSwap;
        this.startingCore = startingCore;
        this.stickySessions = stickySessions;
        this.sunriseWorkingDir = sunriseWorkingDir == null ? defaultJapanPath() : sunriseWorkingDir;
    }

    @JsonGetter
    public boolean enableMultiControllerIntegration() {
        return enableMultiControllerIntegration;
    }

    @JsonGetter
    public boolean swapMultiControllerAssignmentsOnWindowSwap() {
        return swapMultiControllerAssignmentsOnWindowSwap;
    }

    @JsonGetter
    public boolean stickySessions() {
        return stickySessions;
    }

    @JsonGetter
    public int startingCore() {
        return startingCore;
    }

    public void setStartingCore(int startingCore) {
        this.startingCore = startingCore;
    }

    @JsonSetter
    public void setEnableMultiControllerIntegration(boolean enableMultiControllerIntegration) {
        this.enableMultiControllerIntegration = enableMultiControllerIntegration;
    }

    @JsonSetter
    public void setSwapMultiControllerAssignmentsOnWindowSwap(boolean multiControllerSwaps) {
        this.swapMultiControllerAssignmentsOnWindowSwap = multiControllerSwaps;
    }

    @JsonSetter
    public void setStickySessions(boolean stickySessions) {
        this.stickySessions = stickySessions;
    }

    @JsonGetter
    public Path sunriseWorkingDir() {
        return sunriseWorkingDir;
    }

    @JsonSetter
    public void setSunriseWorkingDir(Path sunriseWorkingDir) {
        this.sunriseWorkingDir = sunriseWorkingDir;
    }

    public static Path defaultJapanPath() {
        var appData = System.getenv("LOCALAPPDATA");
        return Paths.get(appData, "SunriseGames", "Toontown", "sv1.2.39.5", "clients", "Toontown_JP");
    }
}
