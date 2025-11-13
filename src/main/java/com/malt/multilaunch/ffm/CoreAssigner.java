package com.malt.multilaunch.ffm;

import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreAssigner {
    private static final Logger LOG = LoggerFactory.getLogger(CoreAssigner.class);
    private final long[] assignedCores;
    private final int totalCores;

    private CoreAssigner(int totalCores) {
        this.assignedCores = new long[totalCores];
        this.totalCores = totalCores;
    }

    public static CoreAssigner create(int totalCores) {
        return new CoreAssigner(totalCores);
    }

    public static CoreAssigner create() {
        int totalNumCores = Runtime.getRuntime().availableProcessors();
        LOG.debug("Creating core assigner with {} cores", totalNumCores);
        return new CoreAssigner(totalNumCores);
    }

    long[] assignedCores() {
        return assignedCores.clone();
    }

    public synchronized int getNextAvailableCore(long pid) {
        for (int i = 0; i < assignedCores.length; i++) {
            if (assignedCores[i] == 0) {
                assignedCores[i] = pid;
                return i;
            }
        }
        var randomCore = ThreadLocalRandom.current().nextInt(totalCores);
        LOG.warn("There were no cores free to assign from, defaulting to random core {}", randomCore);
        return randomCore;
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
