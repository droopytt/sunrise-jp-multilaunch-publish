package com.malt.multilaunch.model;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.malt.multilaunch.launcher.Server;
import java.nio.file.Path;
import java.nio.file.Paths;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private boolean enableMultiControllerIntegration;
    private boolean swapMultiControllerAssignmentsOnWindowSwap;
    private int startingCore;
    private boolean stickySessions;
    private int volumePercentage;
    private Path jpWorkingDir;
    private Path sunrise2004WorkingDir;
    private Path final2013WorkingDir;
    private String lastSelectedServer;
    private Path test2012WorkingDir;
    private Path brazilWorkingDir;

    public Config(
            @JsonProperty("enableMultiControllerIntegration") boolean enableMultiControllerIntegration,
            @JsonProperty("swapMultiControllerAssignmentsOnWindowSwap")
                    boolean swapMultiControllerAssignmentsOnWindowSwap,
            @JsonProperty("startingCore") int startingCore,
            @JsonProperty("stickySessions") boolean stickySessions,
            @JsonProperty("volumePercentage") int volumePercentage,
            @JsonProperty("lastSelectedServer") String lastSelectedServer,
            @JsonProperty("sunriseJpWorkingDir") Path jpWorkingDir,
            @JsonProperty("sunrise2004WorkingDir") Path sunrise2004WorkingDir,
            @JsonProperty("final2013WorkingDir") Path final2013WorkingDir,
            @JsonProperty("test2012WorkingDir") Path test2012WorkingDir,
            @JsonProperty("brazilWorkingDir") Path brazilWorkingDir) {
        this.enableMultiControllerIntegration = enableMultiControllerIntegration;
        this.swapMultiControllerAssignmentsOnWindowSwap = swapMultiControllerAssignmentsOnWindowSwap;
        this.startingCore = startingCore;
        this.stickySessions = stickySessions;
        this.volumePercentage = volumePercentage;
        this.lastSelectedServer = lastSelectedServer == null ? Server.SUNRISE_JP.canonicalName() : lastSelectedServer;
        this.jpWorkingDir = jpWorkingDir == null ? defaultJapanPath() : jpWorkingDir;
        this.sunrise2004WorkingDir = sunrise2004WorkingDir == null ? defaultSunrise2004Path() : sunrise2004WorkingDir;
        this.final2013WorkingDir = final2013WorkingDir == null ? defaultFinal2013Path() : final2013WorkingDir;
        this.test2012WorkingDir = test2012WorkingDir == null ? defaultTest2012Path() : test2012WorkingDir;
        this.brazilWorkingDir = brazilWorkingDir == null ? defaultBrazilPath() : brazilWorkingDir;
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

    @JsonGetter
    public int volumePercentage() {
        return volumePercentage;
    }

    public void setStartingCore(int startingCore) {
        this.startingCore = startingCore;
    }

    @JsonSetter
    public void setVolumePercentage(int volumePercentage) {
        this.volumePercentage = volumePercentage;
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
    public Path jpWorkingDir() {
        return jpWorkingDir;
    }

    @JsonSetter
    public void setJpWorkingDir(Path jpWorkingDir) {
        this.jpWorkingDir = jpWorkingDir;
    }

    @JsonGetter
    public Path test2012WorkingDir() {
        return test2012WorkingDir;
    }

    @JsonSetter
    public void setTest2012WorkingDir(Path test2012WorkingDir) {
        this.test2012WorkingDir = test2012WorkingDir;
    }

    @JsonGetter
    public Path brazilWorkingDir() {
        return brazilWorkingDir;
    }

    @JsonSetter
    public void setBrazilWorkingDir(Path brazilWorkingDir) {
        this.brazilWorkingDir = brazilWorkingDir;
    }

    @JsonGetter
    public Path sunrise2004WorkingDir() {
        return sunrise2004WorkingDir;
    }

    @JsonSetter
    public void setSunrise2004WorkingDir(Path sunrise2004WorkingDir) {
        this.sunrise2004WorkingDir = sunrise2004WorkingDir;
    }

    @JsonGetter
    public Path final2013WorkingDir() {
        return final2013WorkingDir;
    }

    @JsonGetter
    public String lastSelectedServer() {
        return lastSelectedServer;
    }

    @JsonSetter
    public void setLastSelectedServer(String lastSelectedServer) {
        this.lastSelectedServer = lastSelectedServer;
    }

    public static Path defaultSunrise2004Path() {
        var appData = System.getenv("LOCALAPPDATA");
        return Paths.get(appData, "SunriseGames", "Test Toontown 2004 (sv1.0.10.6.test)");
    }

    public static Path defaultJapanPath() {
        var appData = System.getenv("LOCALAPPDATA");
        return Paths.get(appData, "SunriseGames", "Toontown", "sv1.2.39.5", "clients", "Toontown_JP");
    }

    public static Path defaultBrazilPath() {
        var appData = System.getenv("LOCALAPPDATA");
        return Paths.get(
                appData,
                "SunriseGames",
                "Toontown",
                "sv1.4.40.32",
                "Toontown",
                "sv1.4.40.32",
                "clients",
                "Toontown_BR");
    }

    public static Path defaultFinal2013Path() {
        return Paths.get("C:", "Program Files (x86)", "Disney", "Disney Online", "ToontownOnline");
    }

    public static Path defaultTest2012Path() {
        return Paths.get("C:", "Program Files (x86)", "Disney", "Disney Online", "ToontownOnline_TEST");
    }
}
