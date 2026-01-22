package com.malt.multilaunch.ui;

import com.google.inject.Inject;
import com.malt.multilaunch.Main;
import com.malt.multilaunch.hotkeys.HotkeyService;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.login.AccountService;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.servers.Server;
import com.malt.multilaunch.window.WindowSwapService;
import com.malt.multilaunch.window.WindowUtils;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UltiLauncher extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(UltiLauncher.class);

    // UI elements
    public static final int LOGIN_COLUMN = 0;
    public static final int TOON_COLUMN = 1;
    public static final int END_COLUMN = 2;
    public static final int AUDIO_COLUMN = 3;
    public static final String WINDOW_TITLE = "Ultilaunch";
    public static final Color GREEN = new Color(0, 128, 0);
    public static final Color STICKY_SESSIONS_OFFLINE_COLOR = new Color(247, 150, 25);

    private Thread shutdownHook;

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JButton playButton;
    private JMenuBar menuBar;
    private JMenu serverMenu;
    private JMenuItem optionsMenuItem;
    private JMenuItem endAllMenuItem;
    private JMenuItem untickAllMenuItem;
    private JMenuItem addAccountMenuItem;

    // Member fields
    private final Config config;
    private final Launcher<?> launcher;
    private final ActiveAccountManager activeAccountManager;
    private final WindowSwapService windowSwapService;
    protected final MultiControllerService multiControllerService;
    private final AccountService accountService;
    private final HotkeyService hotkeyService;
    private final ConfigService configService;
    private boolean sessionLocked;

    @Inject
    public UltiLauncher(
            ConfigService configService,
            Config config,
            MultiControllerService multiControllerService,
            ActiveAccountManager activeAccountManager,
            WindowSwapService windowSwapService,
            Launcher<?> launcher,
            AccountService accountService,
            HotkeyService hotkeyService) {
        this.configService = configService;
        this.config = config;
        this.multiControllerService = multiControllerService;
        this.activeAccountManager = activeAccountManager;
        this.windowSwapService = windowSwapService;
        this.launcher = launcher;
        this.accountService = accountService;
        this.hotkeyService = hotkeyService;
    }

    public void initialize() {
        validateWorkingPath();
        initComponents();
        loadAccountsIntoTable(accountService.getLoadedAccounts());
        setupListeners();

        hotkeyService.register();
        windowSwapService.setup();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onWindowClosing();
            }
        });
    }

    private void validateWorkingPath() {}

    private void onWindowClosing() {
        hotkeyService.cleanup();
        windowSwapService.shutdown();

        LOG.debug("Saving accounts before exit...");
        var accountFilePath = Path.of(launcher.getClass().getSimpleName() + "_accounts.json");
        try {
            accountService.saveAccounts(accountFilePath, accountService.getLoadedAccounts());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save accounts to file: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initComponents() {
        setTitle(WINDOW_TITLE);
        setSize(565, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        try {
            setIconImage(ImageIO.read(UltiLauncher.class.getResource("/turban-duke.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        menuBar = new JMenuBar();

        serverMenu = new JMenu("Server");

        var servers = List.of(
                new JCheckBoxMenuItem(Server.SUNRISE_JP.canonicalName(), true),
                new JCheckBoxMenuItem(Server.SUNRISE_2004.canonicalName(), true));

        servers.stream()
                .peek(server -> server.setSelected(false))
                .peek(serverMenu::add)
                .peek(server -> {
                    if (server.getText().equals(launcher.canonicalName())) {
                        server.setSelected(true);
                    }
                })
                .forEach(item -> item.addActionListener(e -> {
                    for (var server : servers) {
                        if (!server.equals(item)) {
                            server.setSelected(false);
                        }
                        item.setSelected(true);
                    }
                    LOG.debug("Selected item {}", item.getText());
                    SwingUtilities.invokeLater(() -> {
                        onWindowClosing();
                        this.dispose();
                        Main.runServerWithName(item.getText());
                    });
                }));

        menuBar.add(serverMenu);

        optionsMenuItem = new JMenuItem("Options");
        menuBar.add(optionsMenuItem);

        endAllMenuItem = new JMenuItem("End All");
        menuBar.add(endAllMenuItem);

        untickAllMenuItem = new JMenuItem("Untick All");
        menuBar.add(untickAllMenuItem);

        addAccountMenuItem = new JMenuItem("Add Account");
        menuBar.add(addAccountMenuItem);

        setJMenuBar(menuBar);

        var columnNames = new String[] {"Login?", "Toon", "End?", "Audio?"};
        tableModel = new DefaultTableModel(columnNames, LOGIN_COLUMN) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == LOGIN_COLUMN || column == END_COLUMN || column == AUDIO_COLUMN) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == LOGIN_COLUMN || column == AUDIO_COLUMN;
            }
        };

        accountTable = new JTable(tableModel);
        var centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer) accountTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
        accountTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        accountTable.setRowHeight(24);
        accountTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        for (int i = 0; i < accountTable.getColumnCount(); i++) {
            if (!accountTable.getColumnClass(i).equals(Boolean.class)) {
                accountTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        accountTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setupCheckboxColumn(accountTable, LOGIN_COLUMN);

        accountTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        accountTable.getColumnModel().getColumn(TOON_COLUMN).setMinWidth(10);
        accountTable.getColumnModel().getColumn(TOON_COLUMN).setPreferredWidth(280);

        UIManager.put("CheckBox.font", new Font("SansSerif", Font.PLAIN, 16));
        SwingUtilities.updateComponentTreeUI(this);

        setupCheckboxColumn(accountTable, END_COLUMN);
        accountTable
                .getColumnModel()
                .getColumn(END_COLUMN)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    var renderer = table.getDefaultRenderer(Boolean.class)
                            .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    if (renderer instanceof JCheckBox checkBox) {
                        checkBox.setOpaque(true);
                        var targetAccount = accountService.getLoadedAccounts().get(row);

                        Color backgroundColor;
                        if (Boolean.TRUE.equals(value)) {
                            backgroundColor = GREEN;
                        } else if (config.stickySessions() && activeAccountManager.isInSession(targetAccount)) {
                            backgroundColor = STICKY_SESSIONS_OFFLINE_COLOR;
                        } else {
                            backgroundColor = Color.RED;
                        }

                        checkBox.setBackground(backgroundColor);
                        checkBox.setSelected(false);
                    }
                    return renderer;
                });

        var scrollPane = new JScrollPane(accountTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        playButton = new JButton("Play");
        playButton.setPreferredSize(new Dimension(0, 30));

        setLayout(new BorderLayout(10, 10));
        add(scrollPane, BorderLayout.CENTER);
        add(playButton, BorderLayout.SOUTH);

        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    private static void setupCheckboxColumn(JTable accountTable, int column) {
        accountTable.getColumnModel().getColumn(column).setMinWidth(70);
        accountTable.getColumnModel().getColumn(column).setPreferredWidth(70);
        accountTable.getColumnModel().getColumn(column).setMaxWidth(90);
    }

    private void setupListeners() {
        playButton.addActionListener(e -> onPlayButtonClicked());

        optionsMenuItem.addActionListener(e -> onOptionsClicked());

        endAllMenuItem.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onEndAllClicked(MouseButton.fromValue(e.getButton()));
            }
        });

        untickAllMenuItem.addActionListener(e -> onUntickAllClicked());

        addAccountMenuItem.addActionListener((e) -> onAddAccountClicked());

        accountTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = accountTable.rowAtPoint(e.getPoint());
                int col = accountTable.columnAtPoint(e.getPoint());
                onTableCellClicked(row, col, MouseButton.fromValue(e.getButton()));
            }
        });
    }

    private void onAddAccountClicked() {
        var newAccount = new Account("", "");

        var dialog = AccountInfoDialog.createWithoutDeleteButton(this, newAccount);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            accountService.addAccount(newAccount);
            tableModel.addRow(new Object[] {newAccount.wantLogin(), newAccount.name(), false});
        }
    }

    private void onPlayButtonClicked() {
        if (accountTable.isEditing()) {
            accountTable.getCellEditor().stopCellEditing();
        }

        var accounts = accountService.getLoadedAccounts();
        for (int i = 0; i < accounts.size(); i++) {
            var value = tableModel.getValueAt(i, LOGIN_COLUMN);
            if (value instanceof Boolean boolValue) {
                accounts.get(i).setWantLogin(boolValue);
            }
        }

        playButton.setEnabled(false);

        CompletableFuture.runAsync(() -> {
            var accountsToLogin = accounts.stream()
                    .filter(account -> {
                        if (config.stickySessions()) {
                            var index = accounts.indexOf(account);
                            return !(boolean) accountTable.getValueAt(index, END_COLUMN);
                        }
                        return activeAccountManager
                                .findProcessForAccount(account)
                                .isEmpty();
                    })
                    .filter(Account::wantLogin)
                    .toList();

            var futures = accountsToLogin.stream()
                    .map(account -> CompletableFuture.runAsync(() -> {
                        var process = launcher.launch(account, launcher.workingDir());
                        activeAccountManager.addProcess(account, process);

                        CompletableFuture.runAsync(() -> {
                                    try {
                                        var exitCode = process.waitFor();
                                        LOG.debug("Process {} exited with code {}", process.pid(), exitCode);
                                    } catch (InterruptedException e) {
                                        throw new RuntimeException(e);
                                    }
                                    deregisterAccount(accounts.indexOf(account), account);
                                    launcher.onProcessEnd(process);
                                })
                                .exceptionally(throwable -> {
                                    LOG.error("Error occurred: {}", throwable.getMessage());
                                    return null;
                                });

                        SwingUtilities.invokeLater(() -> {
                            int row = accounts.indexOf(account);
                            tableModel.setValueAt(true, row, END_COLUMN);
                        });
                    }))
                    .toList();

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                    .orTimeout(30, TimeUnit.SECONDS)
                    .thenRunAsync(() -> playButton.setEnabled(true))
                    .thenRun(() -> launcher.performPostLoginOverrides(accountsToLogin, activeAccountManager, config))
                    .thenRun(() -> {
                        if (config.stickySessions()) {
                            this.sessionLocked = true;
                        }
                    })
                    .exceptionally(throwable -> {
                        LOG.error("Error occurred {}", throwable.getMessage());
                        return null;
                    });
        });
    }

    private void onOptionsClicked() {
        var dialog = new ConfigDialog(this, config);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            configService.saveConfigToFile(config);
        }
        if (!config.stickySessions()) {
            sessionLocked = false;
        }
    }

    private void onEndAllClicked(MouseButton mouseButton) {
        var accounts = accountService.getLoadedAccounts();

        boolean endSessions = false;
        if (config.stickySessions() && !activeAccountManager.activeSession().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "End all sticky sessions? (No will keep window positions for relaunch)",
                    "End Sessions",
                    JOptionPane.YES_NO_OPTION);
            endSessions = (result == JOptionPane.YES_OPTION);
        }

        for (int i = 0; i < accounts.size(); i++) {
            tableModel.setValueAt(false, i, END_COLUMN);
            var account = accounts.get(i);
            int finalI = i;
            activeAccountManager
                    .findProcessForAccount(account)
                    .ifPresent(process -> endAccount(finalI, process, account, mouseButton));

            if (endSessions) {
                activeAccountManager.endSession(account);
            }
        }

        if (endSessions) {
            activeAccountManager.clear();
            sessionLocked = false;
        }
    }

    private void onUntickAllClicked() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, LOGIN_COLUMN);
        }
    }

    private void onTableCellClicked(int row, int col, MouseButton mouseButton) {
        var accounts = accountService.getLoadedAccounts();
        var valueAt = (boolean) tableModel.getValueAt(row, col);
        if (col == LOGIN_COLUMN) {
            if (sessionLocked && !valueAt) {
                JOptionPane.showMessageDialog(
                        this,
                        "Currently in a sticky session, please end all accounts before marking any accounts to log in");
                tableModel.setValueAt(false, row, col);
                return;
            }
            var account = accounts.get(row);
            account.setWantLogin(valueAt);
        } else if (col == TOON_COLUMN) {
            var account = accounts.get(row);
            var dialog = AccountInfoDialog.createDefault(this, account);
            dialog.setVisible(true);

            if (dialog.isSaved()) {
                tableModel.setValueAt(account.name(), row, TOON_COLUMN);
            }

            if (dialog.shouldDelete()) {
                accounts.remove(row);
                tableModel.removeRow(row);
            }
        } else if (col == END_COLUMN) {
            var account = accounts.get(row);

            activeAccountManager.findProcessForAccount(account).ifPresent(process -> {
                if (process.isAlive()) {
                    endAccount(row, process, account, mouseButton);
                }
            });
        } else if (col == AUDIO_COLUMN) {
            var account = accounts.get(row);
            account.setAudio(valueAt);
        }
    }

    private void endAccount(int row, Process process, Account account, MouseButton mouseButton) {
        if (mouseButton == com.malt.multilaunch.ui.MouseButton.LEFT
                || mouseButton == com.malt.multilaunch.ui.MouseButton.RIGHT) {
            if (mouseButton == com.malt.multilaunch.ui.MouseButton.LEFT) {
                process.destroy();
            } else {
                WindowUtils.sendCloseSignal(process);
            }
            deregisterAccount(row, account);
        }
    }

    private void deregisterAccount(int row, Account account) {
        if (!config.stickySessions()) {
            activeAccountManager.endSession(account);
        } else {
            activeAccountManager.removeAccount(account);
        }
        tableModel.setValueAt(false, row, END_COLUMN);
    }

    private void loadAccountsIntoTable(List<Account> accounts) {
        for (var account : accounts) {
            tableModel.addRow(new Object[] {account.wantLogin(), account.name(), false, account.audio()});
        }
    }
}
