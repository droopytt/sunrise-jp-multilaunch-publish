package com.malt.multilaunch;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.malt.multilaunch.launcher.Server;
import com.malt.multilaunch.launcher.sunrise.brazil.BrazilModule;
import com.malt.multilaunch.launcher.sunrise.final2013.Final2013Module;
import com.malt.multilaunch.launcher.sunrise.jp.JpModule;
import com.malt.multilaunch.launcher.sunrise.test2004.Test2004Module;
import com.malt.multilaunch.launcher.sunrise.test2012.Test2012Module;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.ui.UltiLauncher;
import com.malt.multilaunch.window.DPIUtils;
import javax.swing.*;

public class Main {
    static void main() {
        var configModule = new ConfigModule();
        var injector = Guice.createInjector(configModule);
        var config = injector.getInstance(Config.class);
        var server = config.lastSelectedServer();
        runServerWithName(server);
    }

    public static void runServerWithName(String name) {
        var module = resolveModuleFromName(name);

        DPIUtils.setProcessDPIAware();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        var injector = Guice.createInjector(module, new ConfigModule());

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
                return new JpModule();
            }
            case SUNRISE_2004 -> {
                return new Test2004Module();
            }
            case SUNRISE_FINAL_2013 -> {
                return new Final2013Module();
            }
            case SUNRISE_TEST_2012 -> {
                return new Test2012Module();
            }
            case SUNRISE_BRAZIL -> {
                return new BrazilModule();
            }
        }
        throw new IllegalArgumentException("No module resolvable from name %s".formatted(name));
    }
}
