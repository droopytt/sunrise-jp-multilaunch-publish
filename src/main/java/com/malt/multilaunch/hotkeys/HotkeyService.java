package com.malt.multilaunch.hotkeys;

import static java.util.Objects.nonNull;

import com.malt.multilaunch.login.AccountService;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import com.malt.multilaunch.window.WindowUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Level;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface HotkeyService {
    Logger LOG = LoggerFactory.getLogger(HotkeyService.class);

    static HotkeyService create(
            AccountService accountService,
            Config config,
            ActiveAccountManager activeAccountManager,
            MultiControllerService multiControllerService,
            WindowService windowService) {
        var accountSupplier = (Supplier<List<Account>>) () -> accountService.getLoadedAccounts().stream()
                .filter(acc -> activeAccountManager.findProcessForAccount(acc).isPresent())
                .toList();
        return builder()
                .withActiveAccountManager(activeAccountManager)
                .withResetKey(
                        KeyConstants.toNativeKeyCode(
                                config.hotkeyConfiguration().resetHotkey()),
                        new ResetWindowsAction(
                                config, activeAccountManager, windowService, multiControllerService, accountSupplier))
                .withSnapKey(
                        KeyConstants.toNativeKeyCode(
                                config.hotkeyConfiguration().snapHotkey()),
                        new SnapWindowsAction(config, activeAccountManager, windowService, accountSupplier))
                .build();
    }

    void handleKey(NativeKeyEvent e);

    /**
     * Binds any keybinds and sets up key detection globally
     * This is NOT to be called in tests, and only to be used in production
     */
    void register();

    void cleanup();

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private final Map<Integer, Runnable> mappings = new HashMap<>();
        private ActiveAccountManager activeAccountManager;

        private Builder() {}

        public Builder withResetKey(int resetKey, Runnable action) {
            mappings.put(resetKey, action);
            return this;
        }

        public Builder withSnapKey(int snapKey, Runnable runnable) {
            mappings.put(snapKey, runnable);
            return this;
        }

        public Builder withActiveAccountManager(ActiveAccountManager activeAccountManager) {
            this.activeAccountManager = activeAccountManager;
            return this;
        }

        public HotkeyService build() {
            return new DefaultHotkeyService(this);
        }

        private static class DefaultHotkeyService implements HotkeyService {
            private final Map<Integer, Runnable> mappings;
            private final ActiveAccountManager activeAccountManager;
            private NativeKeyListener nativeKeyListener;

            private DefaultHotkeyService(Builder builder) {
                this.mappings = builder.mappings;
                this.activeAccountManager = builder.activeAccountManager;
            }

            @Override
            public void handleKey(NativeKeyEvent e) {
                var runnable = mappings.get(e.getKeyCode());
                if (nonNull(runnable)) {
                    runnable.run();
                }
            }

            @Override
            public void register() {
                java.util.logging.Logger jnativeLogger = java.util.logging.Logger.getLogger(
                        GlobalScreen.class.getPackage().getName());
                jnativeLogger.setLevel(Level.OFF);
                jnativeLogger.setUseParentHandlers(false);

                try {
                    GlobalScreen.registerNativeHook();
                } catch (NativeHookException e) {
                    LOG.error("Failed to register native hook", e);
                }

                nativeKeyListener = nativeKeyListener(this);
                GlobalScreen.addNativeKeyListener(nativeKeyListener);
            }

            private NativeKeyListener nativeKeyListener(HotkeyService hotkeyService) {
                return new NativeKeyListener() {
                    private boolean altPressed = false;

                    @Override
                    public void nativeKeyPressed(NativeKeyEvent e) {
                        if (!WindowUtils.isToontownWindowActive()) {
                            return;
                        }

                        if (activeAccountManager.activeAccounts().isEmpty()) {
                            return;
                        }

                        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                            altPressed = true;
                        }

                        if (altPressed) {
                            hotkeyService.handleKey(e);
                        }
                    }

                    @Override
                    public void nativeKeyReleased(NativeKeyEvent e) {
                        if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                            altPressed = false;
                        }
                    }

                    @Override
                    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {}
                };
            }

            @Override
            public void cleanup() {
                try {
                    GlobalScreen.unregisterNativeHook();
                    if (nativeKeyListener != null) {
                        GlobalScreen.removeNativeKeyListener(nativeKeyListener);
                    }
                } catch (NativeHookException e) {
                    LOG.error("Failed to unregister native hook", e);
                }
            }
        }
    }
}
