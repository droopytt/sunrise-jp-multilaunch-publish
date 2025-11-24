package com.malt.multilaunch;

import com.google.inject.Guice;
import com.malt.multilaunch.ui.SunriseJpUltiLauncherModule;
import com.malt.multilaunch.ui.UltiLauncher;
import com.malt.multilaunch.window.DPIUtils;
import javax.swing.*;

public class Main {
    static void main() {
        DPIUtils.setProcessDPIAware();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        var injector = Guice.createInjector(new SunriseJpUltiLauncherModule());

        SwingUtilities.invokeLater(() -> {
            var ultiLauncher = injector.getInstance(UltiLauncher.class);
            ultiLauncher.setVisible(true);

            ultiLauncher.initialize();
        });
    }
}
