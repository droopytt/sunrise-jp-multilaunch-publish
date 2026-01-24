package com.malt.multilaunch.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.launcher.launchers.JPLauncher;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.servers.Server;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ConfigService {

    static ConfigService create() {
        return new DefaultConfigService();
    }

    Config load();

    void validate(Config config);

    void saveConfigToFile(Config config);

    class DefaultConfigService implements ConfigService {
        private static final String CONFIG_FILE = "config.json";
        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        private static final Logger LOG = LoggerFactory.getLogger(DefaultConfigService.class);

        private DefaultConfigService() {}

        @Override
        public Config load() {
            var configPath = Path.of(CONFIG_FILE);
            if (!Files.exists(configPath)) {
                try {
                    configPath.toFile().createNewFile();
                    var value = new Config(
                            true,
                            false,
                            1,
                            false,
                            100,
                            Server.SUNRISE_JP.canonicalName(),
                            Config.defaultJapanPath(),
                            Config.defaultSunrise2004Path());
                    OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(configPath.toFile(), value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                var config = OBJECT_MAPPER.readValue(configPath.toFile(), Config.class);
                validate(config);
                return config;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void validate(Config config) {
            validateCoreCount(config);
            validateWorkingDirs(config);
            saveConfigToFile(config);
        }

        private void validateWorkingDirs(Config config) {
            if (!config.jpWorkingDir().equals(Config.defaultJapanPath())) {
                var expectedExecutable = JPLauncher.jpExecutableName();
                try (var files = Files.list(config.jpWorkingDir())) {
                    if (files.map(Path::getFileName).map(Path::toString).noneMatch(p -> p.equals(expectedExecutable))) {
                        adjustWorkingDir(config, expectedExecutable);
                    }
                } catch (IOException e) {
                    adjustWorkingDir(config, expectedExecutable);
                }
            }
        }

        private void adjustWorkingDir(Config config, String expectedExecutable) {
            showMessage(
                    config,
                    "Could not find %s executable in %s. Resetting to default location."
                            .formatted(expectedExecutable, config.jpWorkingDir().toAbsolutePath()));
            config.setJpWorkingDir(Config.defaultJapanPath());
        }

        private void validateCoreCount(Config config) {
            if (config.startingCore() >= Runtime.getRuntime().availableProcessors()) {
                showMessage(
                        config,
                        "You cannot assign %d as a starting core as that core does not exist. Setting to default of 1.");
                config.setStartingCore(1);
            }
        }

        private static void showMessage(Config config, String message) {
            JOptionPane.showMessageDialog(null, message.formatted(config.startingCore()));
        }

        @Override
        public void saveConfigToFile(Config config) {
            var configFilePath = Path.of(CONFIG_FILE);
            try {
                OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(configFilePath.toFile(), config);
                LOG.debug("Config saved");
            } catch (IOException e) {
                LOG.error("Failed to save config", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to save accounts to file: " + e.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
