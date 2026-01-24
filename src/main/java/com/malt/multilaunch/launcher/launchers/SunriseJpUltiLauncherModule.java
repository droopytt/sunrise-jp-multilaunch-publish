package com.malt.multilaunch.launcher.launchers;

import static java.util.stream.Collectors.joining;

import com.google.inject.*;
import com.malt.multilaunch.ffm.CoreAssigner;
import com.malt.multilaunch.hotkeys.HotkeyService;
import com.malt.multilaunch.hotkeys.ResetWindowsAction;
import com.malt.multilaunch.hotkeys.SnapWindowsAction;
import com.malt.multilaunch.launcher.GameLoginClient;
import com.malt.multilaunch.launcher.JpGameLoginClient;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.login.AccountService;
import com.malt.multilaunch.login.SunriseApiResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jnativehook.keyboard.NativeKeyEvent;

public class SunriseJpUltiLauncherModule extends AbstractModule {

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
            GameLoginClient<SunriseApiResponse> gameLoginClient) {
        return new JPLauncher(config, multiControllerService, coreAssigner, windowService, gameLoginClient);
    }

    @Provides
    @Singleton
    public GameLoginClient<SunriseApiResponse> gameLoginClient() {
        return new JpGameLoginClient();
    }

    public static String generateFormData(String username, String password, Map<String, String> additionalArgs) {
        var base = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&password="
                + URLEncoder.encode(password, StandardCharsets.UTF_8);
        if (additionalArgs.isEmpty()) {
            return base;
        }
        var additionalArgsAsString = additionalArgs.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(joining("&"));
        return base + "&" + additionalArgsAsString;
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
