package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.hotkeys.KeyConstants;
import com.malt.multilaunch.model.Config;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.swing.*;
import org.jnativehook.keyboard.NativeKeyEvent;

public class KeyCaptureField extends ConfigField<Integer> {

    KeyCaptureField(String key, String label, Function<Config, Integer> getter, BiConsumer<Config, Integer> setter) {
        super(key, label + " ALT +", getter, setter);
    }

    @Override
    JComponent createComponent(Config config) {
        var panel = new JPanel(new BorderLayout(5, 0));

        var textField = new JTextField(15);
        textField.setText(createTextForNativeKey(KeyConstants.toNativeKeyCode(getter.apply(config))));
        textField.setEditable(false);
        textField.setFocusable(true);

        var clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(60, textField.getPreferredSize().height));

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int javaKeyCode = e.getKeyCode();
                int nativeKeyCode = KeyConstants.toNativeKeyCode(javaKeyCode);

                textField.setText(createTextForNativeKey(nativeKeyCode));
                textField.putClientProperty("keyCode", javaKeyCode);
            }
        });

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                textField.setText("Press a key...");
                textField.requestFocusInWindow();
            }
        });

        clearButton.addActionListener(e -> {
            textField.setText("Not set (click to capture)");
            textField.putClientProperty("keyCode", null);
        });

        panel.add(textField, BorderLayout.CENTER);
        panel.add(clearButton, BorderLayout.EAST);

        return panel;
    }

    private static String createTextForNativeKey(int nativeKeyCode) {
        return NativeKeyEvent.getKeyText(nativeKeyCode);
    }

    @Override
    void loadValue(Config config, JComponent component) {
        var panel = (JPanel) component;
        var textField = (JTextField) panel.getComponent(0);
        var keyCode = getter.apply(config);
        if (keyCode != null && keyCode != 0) {
            textField.setText(createTextForNativeKey(KeyConstants.toNativeKeyCode(keyCode)));
            textField.putClientProperty("keyCode", keyCode);
        } else {
            textField.setText("Not set (click to capture)");
        }
    }

    @Override
    void saveValue(Config config, JComponent component) {
        var panel = (JPanel) component;
        var textField = (JTextField) panel.getComponent(0);
        var keyCode = (Integer) textField.getClientProperty("keyCode");
        setter.accept(config, keyCode != null ? keyCode : 0);
    }
}
