package com.malt.multilaunch.launchers;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.hotkeys.HotkeyService;
import com.malt.multilaunch.hotkeys.ResetWindowsAction;
import com.malt.multilaunch.hotkeys.SnapWindowsAction;
import com.malt.multilaunch.launcher.*;
import com.malt.multilaunch.login.AccountService;
import com.malt.multilaunch.login.JpApiResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.ui.ConfigService;
import com.malt.multilaunch.window.WindowService;
import java.util.List;
import java.util.function.Supplier;
import org.jnativehook.keyboard.NativeKeyEvent;

public class Sunrise2004UltiLauncherModule extends AbstractModule {

    @Provides
    @Singleton
    public ConfigService configService() {
        return ConfigService.create();
    }

    @Provides
    @Singleton
    public Config config(ConfigService configService) {
        return configService.load();
    }

    @Provides
    @Singleton
    public MultiControllerService multiControllerService(Config config) {
        return MultiControllerService.createDefault(config);
    }

    @Provides
    @Singleton
    public ActiveAccountManager activeAccountManager() {
        return ActiveAccountManager.create();
    }

    @Provides
    @Singleton
    public WindowService windowService() {
        return WindowService.create();
    }

    @Provides
    @Singleton
    public Launcher<?> launcher(
            Config config,
            MultiControllerService multiControllerService,
            CoreAssigner coreAssigner,
            WindowService windowService,
            GameLoginClient<JpApiResponse> gameLoginClient) {
        return new Sunrise2004Launcher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }

    @Provides
    @Singleton
    public GameLoginClient<JpApiResponse> gameLoginClient() {
        return new Sunrise2004GameLoginClient();
    }

    @Provides
    @Singleton
    public AccountService accountService(Launcher<?> launcher) {
        var accountService = AccountService.create(launcher);
        accountService.loadAccountsFromFile();
        return accountService;
    }

    @Provides
    @Singleton
    public HotkeyService hotkeyService(
            Config config,
            AccountService accountService,
            ActiveAccountManager activeAccountManager,
            MultiControllerService multiControllerService,
            WindowService windowService) {
        var accountSupplier = (Supplier<List<Account>>) () -> accountService.getLoadedAccounts().stream()
                .filter(acc -> activeAccountManager.findProcessForAccount(acc).isPresent())
                .toList();
        return HotkeyService.builder()
                .withActiveAccountManager(activeAccountManager)
                .withHotkeyMapping(
                        NativeKeyEvent.VC_R,
                        new ResetWindowsAction(
                                config, activeAccountManager, windowService, multiControllerService, accountSupplier))
                .withHotkeyMapping(
                        NativeKeyEvent.VC_S,
                        new SnapWindowsAction(config, activeAccountManager, windowService, accountSupplier))
                .build();
    }

    @Provides
    @Singleton
    public CoreAssigner coreAssigner(Config config) {
        return CoreAssigner.createWithStartingCore(config.startingCore());
    }
}
