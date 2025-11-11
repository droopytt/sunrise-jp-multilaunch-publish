package com.malt.multilaunch.ui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.Account;
import com.malt.multilaunch.Config;
import com.malt.multilaunch.jna.WindowUtils;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.launcher.SunriseJPLauncher;
import com.malt.multilaunch.login.APIResponse;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.util.stream.Collectors.joining;

public class UltiLauncher<R extends APIResponse, T extends Launcher<R>> extends JFrame {
    private static final Logger LOG = LoggerFactory.getLogger(UltiLauncher.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // UI elements
    public static final int LOGIN_COLUMN = 0;
    public static final int TOON_COLUMN = 1;
    public static final int END_COLUMN = 2;
    public static final String WINDOW_TITLE = "Ultilaunch";
    public static final String CONFIG_FILE = "config.json";

    private JTable accountTable;
    private DefaultTableModel tableModel;
    private JButton playButton;
    private JMenuBar menuBar;
    private JMenu serverMenu;
    private JMenu optionsMenu;
    private JMenuItem sunriseJpMenuItem;
    private JMenuItem rewrittenMenuItem;
    private JMenuItem optionsMenuItem;
    private JMenuItem endAllMenuItem;
    private JMenuItem untickAllMenuItem;
    private JMenuItem addAccountMenuItem;

    // Member fields
    private final Config config;
    private Launcher<?> launcher;
    private final List<Account> accounts;
    private final ActiveAccountManager activeAccountManager;
    private final WindowSwapService windowSwapService;
    protected final MultiControllerService multiControllerService;

    public UltiLauncher() {
        this.config = readConfig();
        multiControllerService = MultiControllerService.createDefault(config);
        launcher = new SunriseJPLauncher(resolvePath(), multiControllerService);
        accounts = findAccounts(launcher);
        activeAccountManager = ActiveAccountManager.create();
        windowSwapService = new WindowSwapService(activeAccountManager, multiControllerService);

        initComponents();
        loadAccounts(accounts);
        setupListeners();
        setupGlobalHotkeys();
        setupShutdownHook();
        windowSwapService.setup();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                onWindowClosing();
            }
        });
    }

    private Config readConfig() {
        var configPath = Path.of(CONFIG_FILE);
        if (!Files.exists(configPath)) {
            try {
                configPath.toFile().createNewFile();
                var value = new Config(true, false);
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return OBJECT_MAPPER.readValue(configPath.toFile(), Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.debug("Saving accounts before exit...");
            saveAccountsToFile();
        }));
    }

    private void onWindowClosing() {
        cleanupGlobalHotkeys();
        windowSwapService.shutdown();
    }

    private Path resolvePath() {
        var appData = System.getenv("LOCALAPPDATA");
        return Paths.get(appData, "SunriseGames", "Toontown", "sv1.2.39.5", "clients", "Toontown_JP");
    }

    private List<Account> findAccounts(Launcher<?> launcher) {
        var accountFilePath = Path.of(launcher.getClass().getSimpleName() + "_accounts.json");
        assertFileExists(accountFilePath, () -> {
            try {
                accountFilePath.toFile().createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            var value = List.of(
                    new Account("account1", "pass2"),
                    new Account("account1", "pass3"),
                    new Account("account1", "pass4"),
                    new Account("account1", "pass5"));
            try {
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(accountFilePath.toFile(), value);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JOptionPane.showMessageDialog(
                    null,
                    "Generated accounts JSON file at %s, please fill this out with your accounts"
                            .formatted(accountFilePath));
            System.exit(0);
        });
        var accounts = new ArrayList<Account>();
        try {
            accounts = OBJECT_MAPPER.readValue(accountFilePath.toFile(), new TypeReference<>() {
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Could not load accounts - ensure JSON format is filled out");
        }
        return accounts;
    }

    public static void assertFileExists(Path path, Runnable onCreateAction) {
        if (!Files.exists(path)) {
            onCreateAction.run();
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
        sunriseJpMenuItem = new JCheckBoxMenuItem("Sunrise JP", true);
        rewrittenMenuItem = new JCheckBoxMenuItem("Rewritten");
        serverMenu.add(sunriseJpMenuItem);
        // TODO reinstate one day
        //        serverMenu.add(rewrittenMenuItem);
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

        var columnNames = new String[]{"Login?", "Toon", "End?"};
        tableModel = new DefaultTableModel(columnNames, LOGIN_COLUMN) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == LOGIN_COLUMN || column == END_COLUMN) {
                    return Boolean.class;
                }
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == LOGIN_COLUMN;
            }
        };

        accountTable = new JTable(tableModel);
        var centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        ((DefaultTableCellRenderer) accountTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < accountTable.getColumnCount(); i++) {
            if (!accountTable.getColumnClass(i).equals(Boolean.class)) {
                accountTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        accountTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setupCheckboxColumn(accountTable, LOGIN_COLUMN);

        accountTable.getColumnModel().getColumn(TOON_COLUMN).setPreferredWidth(150);
        accountTable.getColumnModel().getColumn(TOON_COLUMN).setMaxWidth(150);

        setupCheckboxColumn(accountTable, END_COLUMN);
        accountTable
                .getColumnModel()
                .getColumn(END_COLUMN)
                .setCellRenderer((table, value, isSelected, hasFocus, row, column) -> {
                    var renderer = table.getDefaultRenderer(Boolean.class)
                            .getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    if (renderer instanceof JCheckBox checkBox) {
                        checkBox.setOpaque(true);
                        checkBox.setBackground(Boolean.TRUE.equals(value) ? new Color(0, 200, 0, 80) : Color.RED);
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
        accountTable.getColumnModel().getColumn(column).setMinWidth(40);
        accountTable.getColumnModel().getColumn(column).setPreferredWidth(50);
        accountTable.getColumnModel().getColumn(column).setMaxWidth(55);
    }

    private void setupListeners() {
        playButton.addActionListener(e -> onPlayButtonClicked());

        sunriseJpMenuItem.addActionListener(e -> sunriseJpSelected());
        rewrittenMenuItem.addActionListener(e -> onRewrittenSelected());

        optionsMenuItem.addActionListener(e -> onOptionsClicked());

        endAllMenuItem.addActionListener(e -> onEndAllClicked());

        untickAllMenuItem.addActionListener(e -> onUntickAllClicked());

        addAccountMenuItem.addActionListener((e) -> onAddAccountClicked());

        accountTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = accountTable.rowAtPoint(e.getPoint());
                int col = accountTable.columnAtPoint(e.getPoint());
                onTableCellClicked(row, col);
            }
        });
    }

    private void setupGlobalHotkeys() {
        java.util.logging.Logger jnativeLogger = java.util.logging.Logger.getLogger(
                GlobalScreen.class.getPackage().getName());
        jnativeLogger.setLevel(Level.OFF);
        jnativeLogger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            LOG.error("Failed to register native hook", e);
            return;
        }

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            private boolean altPressed = false;

            @Override
            public void nativeKeyPressed(NativeKeyEvent e) {
                if (!WindowUtils.isToontownWindowActive()) {
                    return;
                }

                if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                    altPressed = true;
                }

                if (altPressed) {
                    switch (e.getKeyCode()) {
                        case NativeKeyEvent.VC_R:
                            var processes = accounts.stream()
                                    .map(activeAccountManager::findProcessForAccount)
                                    .flatMap(Optional::stream)
                                    .toList();
                            activeAccountManager.resetWindows();
                            Launcher.resizeWindowsForProcesses(
                                    accounts.stream()
                                            .filter(acc -> activeAccountManager
                                                    .findProcessForAccount(acc)
                                                    .isPresent())
                                            .toList(),
                                    activeAccountManager);
                            launcher.reassignControllersForProcesses(processes);
                            break;
                        case NativeKeyEvent.VC_S:
                            Launcher.resizeWindowsWithRectangles(accounts, activeAccountManager,
                                    activeAccountManager.accounts().stream()
                                            .sorted((a1, a2) -> Integer.compare(accounts.indexOf(a1), accounts.indexOf(a2)))
                                            .map(activeAccountManager::findWindowRect)
                                            .flatMap(Optional::stream)
                                            .toList());
                            break;
                    }
                }
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent e) {
                if (e.getKeyCode() == NativeKeyEvent.VC_ALT_L || e.getKeyCode() == NativeKeyEvent.VC_ALT_R) {
                    altPressed = false;
                }
            }

            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
            }
        });
    }

    private void cleanupGlobalHotkeys() {
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            LOG.error("Failed to unregister native hook", e);
        }
    }

    private void onAddAccountClicked() {
        var newAccount = new Account("", "");

        var dialog = AccountInfoDialog.createWithoutDeleteButton(this, newAccount);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            accounts.add(newAccount);
            tableModel.addRow(new Object[]{newAccount.wantLogin(), newAccount.name(), false});
        }
    }

    private void saveAccountsToFile() {
        var accountFilePath = Path.of(launcher.getClass().getSimpleName() + "_accounts.json");
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(accountFilePath.toFile(), accounts);
            LOG.debug("Accounts saved to file");
        } catch (IOException e) {
            LOG.error("Failed to save accounts", e);
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save accounts to file: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onPlayButtonClicked() {
        if (accountTable.isEditing()) {
            accountTable.getCellEditor().stopCellEditing();
        }

        for (int i = 0; i < accounts.size(); i++) {
            var value = tableModel.getValueAt(i, LOGIN_COLUMN);
            if (value instanceof Boolean boolValue) {
                accounts.get(i).setWantLogin(boolValue);
            }
        }

        LOG.debug(
                "Accounts are {}",
                accounts.stream()
                        .map(a -> "(" + a.username() + ", " + a.wantLogin() + ")")
                        .collect(joining(", ")));

        playButton.setEnabled(false);

        CompletableFuture.runAsync(() -> {
            var accountsToLogin = accounts.stream()
                    .filter(account ->
                            activeAccountManager.findProcessForAccount(account).isEmpty())
                    .filter(Account::wantLogin)
                    .toList();

            LOG.debug(
                    "Accounts to login are {}",
                    accountsToLogin.stream()
                            .map(a -> "(" + a.username() + ", " + a.wantLogin() + ")")
                            .collect(joining(", ")));
            var futures = accountsToLogin.stream()
                    .map(account -> CompletableFuture.runAsync(() -> {
                        var process = launcher.launch(account, launcher.workingDir());
                        activeAccountManager.addProcess(account, process);

                        CompletableFuture.runAsync(() -> {
                            try {
                                process.waitFor();
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                            deregisterAccount(accounts.indexOf(account), account);
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
                    .thenRun(() -> launcher.performPostLoginOverrides(accountsToLogin, activeAccountManager));
        });
    }

    private void sunriseJpSelected() {
        LOG.info("Sunrise JP server selected");
        rewrittenMenuItem.setSelected(false);
        sunriseJpMenuItem.setSelected(true);
        // TODO: Switch to Sunrise JP server
    }

    private void onRewrittenSelected() {
        LOG.info("Clash server selected");
        sunriseJpMenuItem.setSelected(false);
        rewrittenMenuItem.setSelected(true);
        // TODO: Switch to Rewritten server
    }

    private void onOptionsClicked() {
        var dialog = new ConfigDialog(this, config);
        dialog.setVisible(true);

        if (dialog.isSaved()) {
            saveConfigToFile();
        }
    }

    private void saveConfigToFile() {
        var configFilePath = Path.of(CONFIG_FILE);
        try {
            OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(configFilePath.toFile(), config);
            LOG.debug("Config saved");
        } catch (IOException e) {
            LOG.error("Failed to save config", e);
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to save accounts to file: " + e.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEndAllClicked() {
        for (int i = 0; i < accounts.size(); i++) {
            tableModel.setValueAt(false, i, END_COLUMN);
            var account = accounts.get(i);
            int finalI = i;
            activeAccountManager.findProcessForAccount(account).ifPresent(process -> {
                endAccount(finalI, process, account);
            });
        }
        activeAccountManager.clear();
    }

    private void onUntickAllClicked() {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            tableModel.setValueAt(false, i, LOGIN_COLUMN);
        }
    }

    private void onTableCellClicked(int row, int col) {
        if (col == LOGIN_COLUMN) {
            var account = accounts.get(row);
            var valueAt = (boolean) tableModel.getValueAt(row, col);
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
                    endAccount(row, process, account);
                }
            });
        }
    }

    private void endAccount(int row, Process process, Account account) {
        process.destroy();
        deregisterAccount(row, account);
    }

    private void deregisterAccount(int row, Account account) {
        activeAccountManager.removeAccount(account);
        tableModel.setValueAt(false, row, END_COLUMN);
    }

    private void loadAccounts(List<Account> accounts) {
        for (var account : accounts) {
            tableModel.addRow(new Object[]{account.wantLogin(), account.name(), false});
        }
    }

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
