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

    private final Function<Config, Boolean> enabledGetter;
    private final BiConsumer<Config, Boolean> enabledSetter;

    KeyCaptureField(String key, String label, Function<Config, Integer> getter, BiConsumer<Config, Integer> setter) {
        this(key, label, getter, setter, _ -> false, (_, _) -> {});
    }

    KeyCaptureField(
            String key,
            String label,
            Function<Config, Integer> getter,
            BiConsumer<Config, Integer> setter,
            Function<Config, Boolean> enabledGetter,
            BiConsumer<Config, Boolean> enabledSetter) {
        super(key, label + " ALT +", getter, setter);
        this.enabledGetter = enabledGetter;
        this.enabledSetter = enabledSetter;
    }

    @Override
    JComponent createComponent(Config config) {
        var panel = new JPanel(new BorderLayout(5, 0));

        var textField = new JTextField(15);
        textField.setEditable(false);
        textField.setFocusable(true);

        var enableCheckbox = new JCheckBox("Enabled", true);

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!enableCheckbox.isSelected()) {
                    return;
                }

                int javaKeyCode = e.getKeyCode();
                int nativeKeyCode = KeyConstants.toNativeKeyCode(javaKeyCode);

                textField.setText(createTextForNativeKey(nativeKeyCode));
                textField.putClientProperty("keyCode", javaKeyCode);
            }
        });

        textField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (enableCheckbox.isSelected()) {
                    textField.setText("Press a key...");
                    textField.requestFocusInWindow();
                }
            }
        });

        enableCheckbox.addActionListener(e -> {
            boolean enabled = enableCheckbox.isSelected();
            textField.setEnabled(enabled);
            enabledSetter.accept(config, enabled);
        });

        panel.add(textField, BorderLayout.CENTER);
        panel.add(enableCheckbox, BorderLayout.EAST);

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
        var enabled = enabledGetter.apply(config);
        var checkbox = (JCheckBox) panel.getComponent(1);
        checkbox.setSelected(enabled);
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
