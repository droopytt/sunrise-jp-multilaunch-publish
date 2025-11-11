package com.malt.multilaunch.ui;

import com.malt.multilaunch.Account;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveAccountManager {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveAccountManager.class);
    private final Map<Account, Process> activeProcesses;
    private final Map<Account, Rectangle> windowRects;

    private ActiveAccountManager(Map<Account, Process> activeProcesses, Map<Account, Rectangle> windowRects) {
        this.activeProcesses = activeProcesses;
        this.windowRects = windowRects;
    }

    public static ActiveAccountManager create() {
        return new ActiveAccountManager(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    public void addProcess(Account account, Process process) {
        activeProcesses.put(account, process);
    }

    public void putWindow(Account account, Rectangle rectangle) {
        windowRects.put(account, rectangle);
    }

    public Set<Account> accounts() {
        return activeProcesses.keySet();
    }

    public Collection<Rectangle> allWindows() {
        return windowRects.values();
    }

    public void resetWindows() {
        windowRects.clear();
    }

    public Optional<Rectangle> findWindowRect(Account account) {
        return Optional.ofNullable(windowRects.get(account));
    }

    public Optional<Process> findProcessForAccount(Account account) {
        return Optional.ofNullable(activeProcesses.get(account));
    }

    public void removeAccount(Account account) {
        activeProcesses.remove(account);
        windowRects.remove(account);
    }

    public void clear() {
        activeProcesses.clear();
        ;
        windowRects.clear();
    }
}
