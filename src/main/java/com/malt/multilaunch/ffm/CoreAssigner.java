package com.malt.multilaunch.ffm;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreAssigner {
    private static final Logger LOG = LoggerFactory.getLogger(CoreAssigner.class);
    private final PriorityQueue<Integer> freeCores;
    private final long[] coreToPid;
    private final Map<Long, Integer> pidToCore = new HashMap<>();
    private final int totalCores;
    private int spilloverCoreCount;
    private final int startingCore;

    private CoreAssigner(int totalCores, int startingCore) {
        if (startingCore >= totalCores) {
            throw new IllegalArgumentException(
                    "Cannot have a starting core that is greater than the total number of cores");
        }
        this.startingCore = startingCore;
        LOG.debug("Creating core assigner with {} cores and starting core {}", totalCores, startingCore);
        this.totalCores = totalCores;
        this.spilloverCoreCount = startingCore;

        this.coreToPid = new long[totalCores];

        this.freeCores = new PriorityQueue<>((a, b) -> Integer.compare(rot(a), rot(b)));

        for (int i = 0; i < totalCores; i++) {
            freeCores.add(i);
        }
    }

    private int rot(int core) {
        return (core - startingCore + totalCores) % totalCores;
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
        return this.coreToPid;
    }

    public synchronized int getNextAvailableCore(long pid) {
        Integer core = freeCores.poll();
        if (core != null) {
            pidToCore.put(pid, core);
            coreToPid[core] = pid;
            return core;
        }

        int next = spilloverCoreCount++;
        spilloverCoreCount %= totalCores;
        return next;
    }

    public synchronized void removeAssignedCore(long pid) {
        Integer core = pidToCore.remove(pid);
        if (core != null) {
            coreToPid[core] = 0;
            freeCores.add(core);
        }
    }
}
