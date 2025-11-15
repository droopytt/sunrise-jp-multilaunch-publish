package com.malt.multilaunch;

import com.malt.multilaunch.ui.UltiLauncher;
import javax.swing.*;

public class Main {
    static void main() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            var frame = new UltiLauncher<>();
            frame.setVisible(true);
        });
    }
}
