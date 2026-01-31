package com.malt.multilaunch.ui.config;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
    private static final BiMap<Integer, Integer> JAVA_TO_NATIVE_KEYCODE = HashBiMap.create();

    static {
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_A, NativeKeyEvent.VC_A);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_B, NativeKeyEvent.VC_B);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_C, NativeKeyEvent.VC_C);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_D, NativeKeyEvent.VC_D);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_E, NativeKeyEvent.VC_E);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F, NativeKeyEvent.VC_F);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_G, NativeKeyEvent.VC_G);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_H, NativeKeyEvent.VC_H);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_I, NativeKeyEvent.VC_I);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_J, NativeKeyEvent.VC_J);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_K, NativeKeyEvent.VC_K);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_L, NativeKeyEvent.VC_L);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_M, NativeKeyEvent.VC_M);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_N, NativeKeyEvent.VC_N);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_O, NativeKeyEvent.VC_O);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_P, NativeKeyEvent.VC_P);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_Q, NativeKeyEvent.VC_Q);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_R, NativeKeyEvent.VC_R);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_S, NativeKeyEvent.VC_S);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_T, NativeKeyEvent.VC_T);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_U, NativeKeyEvent.VC_U);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_V, NativeKeyEvent.VC_V);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_W, NativeKeyEvent.VC_W);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_X, NativeKeyEvent.VC_X);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_Y, NativeKeyEvent.VC_Y);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_Z, NativeKeyEvent.VC_Z);

        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_0, NativeKeyEvent.VC_0);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_1, NativeKeyEvent.VC_1);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_2, NativeKeyEvent.VC_2);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_3, NativeKeyEvent.VC_3);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_4, NativeKeyEvent.VC_4);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_5, NativeKeyEvent.VC_5);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_6, NativeKeyEvent.VC_6);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_7, NativeKeyEvent.VC_7);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_8, NativeKeyEvent.VC_8);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_9, NativeKeyEvent.VC_9);

        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F1, NativeKeyEvent.VC_F1);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F2, NativeKeyEvent.VC_F2);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F3, NativeKeyEvent.VC_F3);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F4, NativeKeyEvent.VC_F4);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F5, NativeKeyEvent.VC_F5);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F6, NativeKeyEvent.VC_F6);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F7, NativeKeyEvent.VC_F7);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F8, NativeKeyEvent.VC_F8);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F9, NativeKeyEvent.VC_F9);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F10, NativeKeyEvent.VC_F10);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F11, NativeKeyEvent.VC_F11);
        JAVA_TO_NATIVE_KEYCODE.put(KeyEvent.VK_F12, NativeKeyEvent.VC_F12);
    }

    public static int toNativeKeyCode(int javaKeyCode) {
        return JAVA_TO_NATIVE_KEYCODE.getOrDefault(javaKeyCode, javaKeyCode);
    }

    public static int toJavaKeyCode(int nativeKeyCode) {
        return JAVA_TO_NATIVE_KEYCODE.inverse().getOrDefault(nativeKeyCode, nativeKeyCode);
    }

    KeyCaptureField(String key, String label, Function<Config, Integer> getter, BiConsumer<Config, Integer> setter) {
        super(key, label + " ALT +", getter, setter);
    }

    @Override
    JComponent createComponent(Config config) {
        var panel = new JPanel(new BorderLayout(5, 0));

        var textField = new JTextField(15);
        textField.setText(createTextForNativeKey(toNativeKeyCode(getter.apply(config))));
        textField.setEditable(false);
        textField.setFocusable(true);

        var clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(60, textField.getPreferredSize().height));

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int javaKeyCode = e.getKeyCode();
                int nativeKeyCode = toNativeKeyCode(javaKeyCode);

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
            textField.setText(createTextForNativeKey(toNativeKeyCode(keyCode)));
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
