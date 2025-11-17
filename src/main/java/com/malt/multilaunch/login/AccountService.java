package com.malt.multilaunch.login;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.launcher.Launcher;
import com.malt.multilaunch.model.Account;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface AccountService {
    List<Account> findAccounts();

    void saveAccounts(Path path, List<Account> accounts) throws IOException;

    static AccountService create(Launcher<? extends APIResponse> launcher) {
        return new DefaultAccountService(launcher);
    }

    class DefaultAccountService implements AccountService {
        private static final Logger LOG = LoggerFactory.getLogger(DefaultAccountService.class);
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private final Launcher<? extends APIResponse> launcher;

        public DefaultAccountService(Launcher<? extends APIResponse> launcher) {
            this.launcher = launcher;
        }

        @Override
        public List<Account> findAccounts() {
            var accountFilePath = Path.of(launcher.getClass().getSimpleName() + "_accounts.json");
            var initialAccounts = List.of(new Account("account1", "pass1"));
            var accounts = initialAccounts;
            assertFileExists(accountFilePath, () -> {
                try {
                    accountFilePath.toFile().createNewFile();
                    OBJECT_MAPPER
                            .writerWithDefaultPrettyPrinter()
                            .writeValue(accountFilePath.toFile(), initialAccounts);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            try {
                accounts = OBJECT_MAPPER.readValue(accountFilePath.toFile(), new TypeReference<>() {});
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Could not load accounts - ensure JSON format is filled out");
            }
            return accounts;
        }

        @Override
        public void saveAccounts(Path path, List<Account> accounts) throws IOException {
            try {
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), accounts);
                LOG.debug("Accounts saved to file");
            } catch (IOException e) {
                LOG.error("Failed to save accounts", e);
                throw e;
            }
        }

        private static void assertFileExists(Path path, Runnable onCreateAction) {
            if (!Files.exists(path)) {
                onCreateAction.run();
            }
        }
    }
}
