package com.malt.multilaunch.ui;

import com.malt.multilaunch.model.Account;
import java.awt.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveAccountManager {
    private final Map<Account, Process> activeProcesses;
    private final Map<Account, Rectangle> windowRects;
    private final Set<Account> activeSession;

    private ActiveAccountManager(
            Map<Account, Process> activeProcesses, Map<Account, Rectangle> windowRects, Set<Account> activeSession) {
        this.activeProcesses = activeProcesses;
        this.windowRects = windowRects;
        this.activeSession = activeSession;
    }

    public static ActiveAccountManager create() {
        return new ActiveAccountManager(
                new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), ConcurrentHashMap.newKeySet());
    }

    public void addProcess(Account account, Process process) {
        activeProcesses.put(account, process);
        activeSession.add(account);
    }

    public void putWindow(Account account, Rectangle rectangle) {
        windowRects.put(account, rectangle);
    }

    public Set<Account> activeSession() {
        return Collections.unmodifiableSet(activeSession);
    }

    public boolean isInSession(Account account) {
        return activeSession.contains(account);
    }

    public Set<Account> accounts() {
        return activeProcesses.keySet();
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

    public void endSession(Account account) {
        activeProcesses.remove(account);
        activeSession.remove(account);
        windowRects.remove(account);
    }

    public void removeAccount(Account account) {
        activeProcesses.remove(account);
    }

    public void clear() {
        activeProcesses.clear();
        activeSession.clear();
        windowRects.clear();
    }

    public void endAllSessions() {
        clear();
    }
}
