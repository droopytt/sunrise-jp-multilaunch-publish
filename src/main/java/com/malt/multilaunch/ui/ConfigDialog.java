package com.malt.multilaunch.ui;

import com.malt.multilaunch.Config;
import java.awt.*;
import javax.swing.*;

public class ConfigDialog extends JDialog {
    private final Config config;
    private JCheckBox multiControllerIntegrationCheckbox;
    private JCheckBox moveControllerAssignmentsWithSwaps;
    private JButton saveButton;

    private boolean saved = false;

    public ConfigDialog(JFrame parent, Config config) {
        super(parent, "Options", true);
        this.config = config;

        initComponents();
        populateFields();
        setupListeners();

        setSize(340, 180);
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
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 10, 10, 10);
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(0, 30));
        add(saveButton, gbc);
    }

    private void populateFields() {
        multiControllerIntegrationCheckbox.setSelected(config.enableMultiControllerIntegration());
        moveControllerAssignmentsWithSwaps.setSelected(config.swapMultiControllerAssignmentsOnWindowSwap());
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> onSaveClicked());
        getRootPane().setDefaultButton(saveButton);
    }

    private void onSaveClicked() {
        var multiControllerIntegration = multiControllerIntegrationCheckbox.isSelected();
        var multiControllerSwaps = moveControllerAssignmentsWithSwaps.isSelected();
        config.setEnableMultiControllerIntegration(multiControllerIntegration);
        config.setSwapMultiControllerAssignmentsOnWindowSwap(multiControllerSwaps);
        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }
}
