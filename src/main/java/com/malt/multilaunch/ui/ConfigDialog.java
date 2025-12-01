package com.malt.multilaunch.ui;

import com.malt.multilaunch.model.Config;
import java.awt.*;
import javax.swing.*;

public class ConfigDialog extends JDialog {
    private final Config config;
    private JCheckBox multiControllerIntegrationCheckbox;
    private JCheckBox moveControllerAssignmentsWithSwaps;
    private JCheckBox stickySessions;
    private JTextField startingCore;
    private JButton saveButton;

    private boolean saved = false;

    public ConfigDialog(JFrame parent, Config config) {
        super(parent, "Options", true);
        this.config = config;

        initComponents();
        populateFields();
        setupListeners();

        setSize(340, 240);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        add(new JLabel("Enable multicontroller integration:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        multiControllerIntegrationCheckbox = new JCheckBox();
        add(multiControllerIntegrationCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("Swap multicontroller assignments on window swap:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        moveControllerAssignmentsWithSwaps = new JCheckBox();
        add(moveControllerAssignmentsWithSwaps, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(new JLabel("Enable sticky sessions"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        stickySessions = new JCheckBox();
        add(stickySessions, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        add(new JLabel("Starting core for affinity assignment (Requires restart):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        startingCore = new JTextField();
        add(startingCore, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(0, 30));
        add(saveButton, gbc);
    }

    private void populateFields() {
        multiControllerIntegrationCheckbox.setSelected(config.enableMultiControllerIntegration());
        moveControllerAssignmentsWithSwaps.setSelected(config.swapMultiControllerAssignmentsOnWindowSwap());
        stickySessions.setSelected(config.stickySessions());
        startingCore.setText(Integer.toString(config.startingCore()));
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> onSaveClicked());
        getRootPane().setDefaultButton(saveButton);
    }

    private void onSaveClicked() {
        config.setEnableMultiControllerIntegration(multiControllerIntegrationCheckbox.isSelected());
        config.setSwapMultiControllerAssignmentsOnWindowSwap(moveControllerAssignmentsWithSwaps.isSelected());
        config.setStickySessions(stickySessions.isSelected());
        try {
            config.setStartingCore(Integer.parseInt(startingCore.getText()));
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not parse value for starting core %s. Value unchanged.".formatted(startingCore.getText()));
        }
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
