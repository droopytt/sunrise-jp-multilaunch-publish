package com.malt.multilaunch;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.malt.multilaunch.launchers.Sunrise2004UltiLauncherModule;
import com.malt.multilaunch.launchers.SunriseJpUltiLauncherModule;
import com.malt.multilaunch.servers.Server;
import com.malt.multilaunch.ui.UltiLauncher;
import com.malt.multilaunch.window.DPIUtils;
import javax.swing.*;

public class Main {
    static void main() {
        runServerWithName(Server.SUNRISE_JP.canonicalName());
    }

    public static void runServerWithName(String name) {
        var module = resolveModuleFromName(name);

        DPIUtils.setProcessDPIAware();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        var injector = Guice.createInjector(module);

        SwingUtilities.invokeLater(() -> {
            var ultiLauncher = injector.getInstance(UltiLauncher.class);
            ultiLauncher.setVisible(true);

            ultiLauncher.initialize();
        });
    }

    private static AbstractModule resolveModuleFromName(String name) {
        var server = Server.fromName(name);
        switch (server) {
            case SUNRISE_JP -> {
                return new SunriseJpUltiLauncherModule();
            }
            case SUNRISE_2004 -> {
                return new Sunrise2004UltiLauncherModule();
            }
        }
        throw new IllegalArgumentException("No module resolvable from name %s".formatted(name));
    }
}
