package com.malt.multilaunch.ui;

import com.malt.multilaunch.Account;
import java.awt.*;
import javax.swing.*;

public class AccountInfoDialog extends JDialog {
    private JTextField toonField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton saveButton;
    private JButton deleteButton;
    private final Account account;

    private boolean saved = false;
    private boolean delete = false;

    private AccountInfoDialog(JFrame parent, Account account, boolean displayDeleteButton) {
        super(parent, "Info", true);
        this.account = account;

        initComponents(displayDeleteButton);
        populateFields();
        setupListeners();

        setSize(340, 180);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    public static AccountInfoDialog createDefault(JFrame parent, Account account) {
        return new AccountInfoDialog(parent, account, true);
    }

    public static AccountInfoDialog createWithoutDeleteButton(JFrame parent, Account account) {
        return new AccountInfoDialog(parent, account, false);
    }

    private void initComponents(boolean displayDeleteButton) {
        setLayout(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        add(new JLabel("Toon:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        toonField = new JTextField();
        add(toonField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        usernameField = new JTextField();
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        passwordField = new JPasswordField();
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 10, 10, 10);
        deleteButton = new JButton("Delete");
        deleteButton.setPreferredSize(new Dimension(0, 30));
        add(deleteButton, gbc);
        if (!displayDeleteButton) {
            deleteButton.setVisible(false);
        }

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(15, 10, 10, 10);
        saveButton = new JButton("Save");
        saveButton.setPreferredSize(new Dimension(0, 30));
        add(saveButton, gbc);
    }

    private void populateFields() {
        if (account != null) {
            toonField.setText(account.name() != null ? account.name() : "");
            usernameField.setText(account.username() != null ? account.username() : "");
            passwordField.setText(account.password() != null ? account.password() : "");
        }
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> onSaveClicked());
        deleteButton.addActionListener(e -> onDeleteClicked());
        getRootPane().setDefaultButton(saveButton);
    }

    private void onDeleteClicked() {
        delete = true;
        dispose();
    }

    private void onSaveClicked() {
        var toon = toonField.getText().trim();
        var username = usernameField.getText().trim();
        var password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Username cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this, "Password cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        account.setName(toon);
        account.setUsername(username);
        account.setPassword(password);

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public boolean shouldDelete() {
        return delete;
    }

    public Account getAccount() {
        return account;
    }
}
