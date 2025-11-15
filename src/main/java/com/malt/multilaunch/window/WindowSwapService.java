package com.malt.multilaunch.window;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.jnativehook.GlobalScreen;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowSwapService {
    private static final Logger LOG = LoggerFactory.getLogger(WindowSwapService.class);
    private final ActiveAccountManager activeAccountManager;
    private final MultiControllerService multiControllerService;
    private AtomicReference<Account> firstSelectedAccount = new AtomicReference<>();
    private ScheduledExecutorService swapScheduler = newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> swapTimeout = null;
    private NativeMouseListener nativeMouseListener;
    private NativeKeyListener nativeAltKeyListener;

    public WindowSwapService(ActiveAccountManager activeAccountManager, MultiControllerService multiControllerService) {
        this.activeAccountManager = activeAccountManager;
        this.multiControllerService = multiControllerService;
    }

    private volatile boolean altDown = false;

    public void setup() {
        nativeAltKeyListener = new NativeKeyListener() {
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                    altDown = true;
                }
            }

            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                    altDown = false;
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}
        };

        nativeMouseListener = new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {}

            @Override
            public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
                if (altDown) {
                    LOG.trace("Got alt click event {}. {}", nativeMouseEvent.getX(), nativeMouseEvent.getY());
                    var x = nativeMouseEvent.getX();
                    var y = nativeMouseEvent.getY();
                    handleAltClick(x, y);
                }
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {}
        };
        GlobalScreen.addNativeMouseListener(nativeMouseListener);
        GlobalScreen.addNativeKeyListener(nativeAltKeyListener);
    }

    private void handleAltClick(int x, int y) {
        Account selectedAccount = null;
        for (var account : activeAccountManager.accounts()) {
            var maybeRectangle = activeAccountManager.findWindowRect(account);
            var maybeProcess = activeAccountManager.findProcessForAccount(account);
            if (maybeRectangle.isPresent() && maybeProcess.isPresent()) {
                var rectangle = maybeRectangle.get();
                if (x >= rectangle.x
                        && x < rectangle.x + rectangle.width
                        && y >= rectangle.y
                        && y < rectangle.y + rectangle.height) {
                    selectedAccount = account;
                    break;
                }
            }
        }

        if (selectedAccount == null) {
            return;
        }

        var acct1 = firstSelectedAccount.get();
        if (acct1 == null) {
            firstSelectedAccount.set(selectedAccount);

            if (swapTimeout != null) {
                swapTimeout.cancel(false);
            }

            swapTimeout = swapScheduler.schedule(() -> firstSelectedAccount.set(null), 3, TimeUnit.SECONDS);
        } else {
            swapWindows(acct1, selectedAccount);
            firstSelectedAccount.set(null);
            if (swapTimeout != null) {
                swapTimeout.cancel(false);
            }
        }
    }

    private void swapWindows(Account acct1, Account acct2) {
        var hwnd1 = activeAccountManager
                .findProcessForAccount(acct1)
                .map(p -> WindowUtils.getWindowHandleForProcessId(p.pid()))
                .orElseThrow();
        var hwnd2 = activeAccountManager
                .findProcessForAccount(acct2)
                .map(p -> WindowUtils.getWindowHandleForProcessId(p.pid()))
                .orElseThrow();

        var rect1 = activeAccountManager.findWindowRect(acct1).orElseThrow();
        var rect2 = activeAccountManager.findWindowRect(acct2).orElseThrow();

        int width1 = rect2.width;
        int height1 = rect2.height;
        int width2 = rect1.width;
        int height2 = rect1.height;

        WindowUtils.resize(hwnd1, rect2.x, rect2.y, width2, height2);
        WindowUtils.resize(hwnd2, rect1.x, rect1.y, width1, height1);

        activeAccountManager.putWindow(acct1, rect2);
        activeAccountManager.putWindow(acct2, rect1);

        multiControllerService.swapHandles(hwnd1, hwnd2);

        LOG.debug("Swapped windows {}:{} and {}:{}", acct1, hwnd1, acct2, hwnd2);
    }

    public void shutdown() {
        swapScheduler.shutdown();
        GlobalScreen.removeNativeMouseListener(nativeMouseListener);
        GlobalScreen.removeNativeKeyListener(nativeAltKeyListener);
    }
}
