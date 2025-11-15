package com.malt.multilaunch.launcher;

import static java.util.stream.Collectors.joining;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.login.APIResponse;
import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.ui.ActiveAccountManager;
import com.malt.multilaunch.window.WindowService;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Launcher<T extends APIResponse> {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final Path workingDir;
    protected final MultiControllerService multiControllerService;
    protected final WindowService windowService;

    public Launcher(Path workingDir, MultiControllerService multiControllerService, WindowService windowService) {
        this.workingDir = workingDir;
        this.multiControllerService = multiControllerService;
        this.windowService = windowService;
    }

    public T response(String username, String password) {
        return response(username, password, Map.of());
    }

    public T response(String username, String password, Map<String, String> additionalArgs) {
        try (var client = HttpClient.newHttpClient()) {

            var formData = generateFormData(username, password, additionalArgs);

            var request = HttpRequest.newBuilder(getLoginApiUri())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formData))
                    .build();

            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            var objectMapper = OBJECT_MAPPER;

            var objectReader = objectMapper.readerFor(responseType());
            return objectReader.readValue(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static String generateFormData(String username, String password, Map<String, String> additionalArgs) {
        var base = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&password="
                + URLEncoder.encode(password, StandardCharsets.UTF_8);
        if (additionalArgs.isEmpty()) {
            return base;
        }
        var additionalArgsAsString = additionalArgs.entrySet().stream()
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(joining("&"));
        return base + "&" + additionalArgsAsString;
    }

    protected abstract Class<T> responseType();

    public Process startGame(Path workingDir, Map<String, String> environmentVars) throws IOException {
        var exePath = workingDir.resolve(executableName()).toAbsolutePath().toString();
        var args = Stream.concat(Stream.of(exePath), processArgs().stream()).toList();
        var processBuilder = new ProcessBuilder(args).directory(workingDir.toFile());
        var allEnvironmentVars = processBuilder.environment();
        allEnvironmentVars.putAll(environmentVars);
        return processBuilder.start();
    }

    public Process launch(Account account, Path workingDir) {
        try {
            var response = response(account.username(), account.password(), additionalLoginArgs());
            var environmentVars = getEnvironmentVariables(response);
            return startGame(workingDir, environmentVars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Map<String, String> additionalLoginArgs();

    public abstract void performPostLoginOverrides(List<Account> accounts, ActiveAccountManager activeAccountManager);

    public abstract Map<String, String> getEnvironmentVariables(T response);

    public abstract URI getLoginApiUri();

    public abstract List<String> processArgs();

    public abstract String executableName();

    public Path workingDir() {
        return workingDir;
    }

    public abstract void onProcessEnd(Process process);
}
