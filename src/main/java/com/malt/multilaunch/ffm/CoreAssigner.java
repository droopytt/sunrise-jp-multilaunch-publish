package com.malt.multilaunch.ffm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreAssigner {
    private static final Logger LOG = LoggerFactory.getLogger(CoreAssigner.class);
    private final long[] assignedCores;
    private final int totalCores;
    private int spilloverCoreCount;
    private final int startingCore;

    private CoreAssigner(int totalCores, int startingCore) {
        if (startingCore >= totalCores) {
            throw new IllegalArgumentException(
                    "Cannot have a starting core that is greater than the total number of cores");
        }
        LOG.debug("Creating core assigner with {} cores and starting core {}", totalCores, startingCore);
        this.assignedCores = new long[totalCores];
        this.totalCores = totalCores;
        this.startingCore = startingCore;
        this.spilloverCoreCount = startingCore;
    }

    public static CoreAssigner create(int totalCores) {
        return new CoreAssigner(totalCores, 0);
    }

    public static CoreAssigner create() {
        int totalCores = Runtime.getRuntime().availableProcessors();
        return new CoreAssigner(totalCores, 0);
    }

    public static CoreAssigner createWithStartingCore(int startingCore) {
        int totalNumCores = Runtime.getRuntime().availableProcessors();
        return new CoreAssigner(totalNumCores, startingCore);
    }

    public static CoreAssigner createWithStartingCore(int totalCores, int startingCore) {
        return new CoreAssigner(totalCores, startingCore);
    }

    long[] assignedCores() {
        return assignedCores.clone();
    }

    public synchronized int getNextAvailableCore(long pid) {
        for (int i = startingCore; i < assignedCores.length; i++) {
            if (assignedCores[i] == 0) {
                assignedCores[i] = pid;
                return i;
            }
        }

        for (int i = 0; i < startingCore; i++) {
            if (assignedCores[i] == 0) {
                assignedCores[i] = pid;
                return i;
            }
        }

        var nextCore = spilloverCoreCount++;
        spilloverCoreCount %= totalCores;
        LOG.warn("There were no cores free to assign from, using spillover core {}", nextCore);
        return nextCore;
    }

    public synchronized void removeAssignedCore(long pid) {
        for (int i = 0; i < assignedCores.length; i++) {
            if (pid == assignedCores[i]) {
                assignedCores[i] = 0;
                return;
            }
        }
    }
}
