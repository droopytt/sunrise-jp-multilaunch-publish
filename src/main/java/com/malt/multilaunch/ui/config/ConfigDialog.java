package com.malt.multilaunch.ui.config;

import com.malt.multilaunch.model.Config;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.swing.*;

public class ConfigDialog extends JDialog {
    private final Config config;
    private JTabbedPane tabbedPane;
    private Map<String, JComponent> fieldComponents = new HashMap<>();
    private boolean saved = false;

    private static final java.util.List<ConfigField<?>> GENERAL_FIELDS = List.of(
            new CheckBoxField(
                    "multiControllerIntegration",
                    "Enable multicontroller integration:",
                    Config::enableMultiControllerIntegration,
                    Config::setEnableMultiControllerIntegration),
            new CheckBoxField(
                    "moveControllerAssignments",
                    "Swap multicontroller assignments on window swap:",
                    Config::swapMultiControllerAssignmentsOnWindowSwap,
                    Config::setSwapMultiControllerAssignmentsOnWindowSwap),
            new CheckBoxField(
                    "stickySessions", "Enable sticky sessions", Config::stickySessions, Config::setStickySessions),
            new SpinnerField(
                    "volumePercentage",
                    "Volume % (of max volume) when audio enabled",
                    0,
                    100,
                    Config::volumePercentage,
                    Config::setVolumePercentage),
            new TextFieldField(
                    "startingCore",
                    "Starting core for affinity assignment (Requires restart):",
                    c -> String.valueOf(c.startingCore()),
                    (c, v) -> c.setStartingCore(Integer.parseInt(v))));

    private static final java.util.List<ConfigField<?>> HOTKEY_FIELDS = List.of(
            new KeyCaptureField(
                    "resetKey",
                    "Reset Hotkey:",
                    config -> config.hotkeyConfiguration().resetHotkey(),
                    (c, v) -> c.hotkeyConfiguration().setResetHotkey(v)),
            new KeyCaptureField(
                    "snapKey",
                    "Snap Windows Hotkey:",
                    config -> config.hotkeyConfiguration().snapHotkey(),
                    (c, v) -> c.hotkeyConfiguration().setSnapHotkey(v)));

    public ConfigDialog(JFrame parent, Config config) {
        super(parent, "Options", true);
        this.config = config;

        initComponents();
        populateFields();

        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("General", createFieldPanel(GENERAL_FIELDS));
        tabbedPane.addTab("Hotkeys", createFieldPanel(HOTKEY_FIELDS));

        add(tabbedPane, BorderLayout.CENTER);

        var buttonPanel = new JPanel();
        var saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(100, 30));
        saveButton.addActionListener(e -> onSaveClicked());
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(saveButton);
    }

    private JPanel createFieldPanel(java.util.List<ConfigField<?>> fields) {
        var panel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);

            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0;
            panel.add(new JLabel(field.label), gbc);

            gbc.gridx = 1;
            gbc.weightx = 1;
            var component = field.createComponent(config);
            fieldComponents.put(field.key, component);
            panel.add(component, gbc);
        }

        gbc.gridy = fields.size();
        gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    private void populateFields() {
        for (var entry : fieldComponents.entrySet()) {
            var field = findFieldByKey(entry.getKey());
            if (field != null) {
                field.loadValue(config, entry.getValue());
            }
        }
    }

    private void onSaveClicked() {
        for (var entry : fieldComponents.entrySet()) {
            var field = findFieldByKey(entry.getKey());
            if (field != null) {
                try {
                    field.saveValue(config, entry.getValue());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Error saving %s: %s".formatted(field.label, e.getMessage()),
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        }
        saved = true;
        dispose();
    }

    private ConfigField<?> findFieldByKey(String key) {
        return Stream.concat(GENERAL_FIELDS.stream(), HOTKEY_FIELDS.stream())
                .filter(f -> f.key.equals(key))
                .findFirst()
                .orElse(null);
    }

    public boolean isSaved() {
        return saved;
    }
}
