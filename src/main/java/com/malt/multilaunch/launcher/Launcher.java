package com.malt.multilaunch.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malt.multilaunch.Account;
import com.malt.multilaunch.jna.WindowData;
import com.malt.multilaunch.jna.WindowUtils;
import com.malt.multilaunch.login.APIResponse;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.multicontroller.WindowAssignRequest;
import com.malt.multilaunch.ui.ActiveAccountManager;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Launcher<T extends APIResponse> {
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    private static final double MULTIPLIER = 0.35f;
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    public static final int MAX_REASONABLE_OFFSET = 30;
    private final Path workingDir;
    private final MultiControllerService multiControllerService;

    public Launcher(Path workingDir, MultiControllerService multiControllerService) {
        this.workingDir = workingDir;
        this.multiControllerService = multiControllerService;
    }

    private static void resize(
            List<Account> accounts, ActiveAccountManager activeAccountManager, List<Rectangle> rectangles) {
        if (accounts.size() < 2) {
            return;
        }
        if (rectangles.size() != accounts.size()) {
            LOG.warn(
                    "Had {} rectangles but {} accounts than processes to resize, returning.",
                    rectangles.size(),
                    accounts.size());
        } else {
            for (int i = 0; i < accounts.size(); i++) {
                var finalI = i;
                CompletableFuture.runAsync(() -> {
                    var account = accounts.get(finalI);
                    var process =
                            activeAccountManager.findProcessForAccount(account).orElseThrow();
                    var hwnd = WindowUtils.getWindowHandleForProcessId((int) process.pid());
                    var oldWindowRect = WindowUtils.getWindowRect(hwnd);
                    var next = rectangles.get(finalI);
                    WindowUtils.resize(hwnd, next.x, next.y, next.width, next.height);
                    activeAccountManager.putWindow(account, next);
                    var newWindowRect = WindowUtils.getWindowRect(hwnd);
                    LOG.trace(
                            "Window rect for pid {} was {} and is now {}", process.pid(), oldWindowRect, newWindowRect);
                });
            }
        }
    }

    private static Dimension getOffsets(Process process) {
        var hwnd = WindowUtils.getWindowHandleForProcessId(process.pid());
        var windowRect = WindowUtils.getWindowRect(hwnd);
        var clientRect = WindowUtils.getClientRect(hwnd);
        int borderWidth = (windowRect.right - windowRect.left) - clientRect.right;
        int borderHeight = (windowRect.bottom - windowRect.top) - clientRect.bottom;
        return new Dimension(borderWidth, borderHeight);
    }

    public static List<Rectangle> createTargetWindowRects(Rectangle workingArea, int numWindows, int offset) {
        var windowRects = new ArrayList<Rectangle>();

        if (numWindows == 2) {
            return Launcher.createDualOnlyRects(workingArea, offset);
        }

        if (numWindows % 2 != 0) {
            numWindows++;
        }

        var windowData = WindowData.create(workingArea, numWindows, offset);

        for (int i = 0; i < numWindows / 2; i++) {
            windowRects.add(new Rectangle(
                    windowData.startX() + (i * windowData.incrementX()),
                    windowData.startY(),
                    windowData.width(),
                    windowData.height()));
        }

        for (int i = 0; i < numWindows / 2; i++) {
            windowRects.add(new Rectangle(
                    windowData.startX() + (i * windowData.incrementX()),
                    windowData.incrementY(),
                    windowData.width(),
                    windowData.height()));
        }

        if (windowRects.size() % 4 != 0) {
            return windowRects;
        }

        return Launcher.groupIntoFours(numWindows, windowRects);
    }

    private static List<Rectangle> createDualOnlyRects(Rectangle workingArea, int offset) {
        var windowData = WindowData.create(workingArea, 4, offset);
        var left = new Rectangle(
                windowData.startX(),
                windowData.startY(),
                windowData.width(),
                windowData.height() + windowData.height() + (int) (windowData.height() * (MULTIPLIER / 100f)));
        var right = new Rectangle(
                windowData.startX() + windowData.incrementX() - offset,
                windowData.startY(),
                windowData.width(),
                windowData.height() + (int) (windowData.height() * (MULTIPLIER / 100f)));
        return List.of(left, right);
    }

    private static List<Rectangle> groupIntoFours(int numWindows, List<Rectangle> windowRects) {
        var groupedRects = new ArrayList<List<Rectangle>>();
        for (int i = 0; i < windowRects.size(); i += 2) {
            List<Rectangle> group = new ArrayList<>();
            group.add(windowRects.get(i));
            if (i + 1 < windowRects.size()) {
                group.add(windowRects.get(i + 1));
            }
            groupedRects.add(group);
        }

        int numPartitions = numWindows / 4;
        var partitions = new ArrayList<List<Rectangle>>();
        for (int i = 0; i < numPartitions; i++) {
            partitions.add(new ArrayList<>());
        }

        for (int i = 0; i < groupedRects.size(); i++) {
            var groupedRect = groupedRects.get(i);
            Launcher.addToPartition(partitions.get(i % numPartitions), groupedRect);
        }

        return partitions.stream().flatMap(List::stream).toList();
    }

    private static void addToPartition(List<Rectangle> partition, List<Rectangle> group) {
        partition.addAll(group);
    }

    public T response(String username, String password) {
        try (var client = HttpClient.newHttpClient()) {

            var formData = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) + "&password="
                    + URLEncoder.encode(password, StandardCharsets.UTF_8);

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
            var response = response(account.username(), account.password());
            var environmentVars = getEnvironmentVariables(response);
            return startGame(workingDir, environmentVars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract void performPostLoginOverrides(List<Account> accounts, ActiveAccountManager activeAccountManager);

    public abstract Map<String, String> getEnvironmentVariables(T response);

    public static void resizeWindowsForProcesses(List<Account> accounts, ActiveAccountManager activeAccountManager) {
        var screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration()
                .getBounds();

        var insets = Toolkit.getDefaultToolkit()
                .getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration());

        var workingArea = new Rectangle(
                screen.x + insets.left,
                screen.y + insets.top,
                screen.width - insets.left - insets.right,
                screen.height - insets.top - insets.bottom);

        var testAccount = accounts.stream()
                .map(activeAccountManager::findProcessForAccount)
                .flatMap(Optional::stream)
                .findAny()
                .orElseThrow();
        var offset = Launcher.getOffsets(testAccount).width;

        if (offset > MAX_REASONABLE_OFFSET) {
            LOG.debug("Offset was suspiciously large ({}), using default value of 16", offset);
            offset = 16;
        }

        var rectangles = Launcher.createTargetWindowRects(workingArea, accounts.size(), offset);

        resizeWindowsWithRectangles(accounts, activeAccountManager, rectangles);
    }

    public static void resizeWindowsWithRectangles(
            List<Account> accounts, ActiveAccountManager activeAccountManager, List<Rectangle> rectangles) {
        Launcher.resize(accounts, activeAccountManager, rectangles);
    }

    public void assignControllerToWindows(List<Process> processes) {
        var toonNumber = 1;
        var requests = new ArrayList<WindowAssignRequest>();

        for (var process : processes) {
            var hWnd = WindowUtils.getWindowHandleForProcessId(process.pid());
            if (hWnd == 0) {
                continue;
            }

            int groupNumber = determineGroupNumberFromIndex(toonNumber);
            var pairDirection = toonNumber % 2 == 0
                    ? WindowAssignRequest.PairDirection.RIGHT
                    : WindowAssignRequest.PairDirection.LEFT;
            requests.add(new WindowAssignRequest(groupNumber, hWnd, pairDirection));
            toonNumber++;
        }

        multiControllerService.sendAssignRequestsToController(requests);
    }

    private static int determineGroupNumberFromIndex(int toonNumber) {
        return (int) Math.ceil(toonNumber / 2.0);
    }

    public abstract URI getLoginApiUri();

    public abstract List<String> processArgs();

    public abstract String executableName();

    public Path workingDir() {
        return workingDir;
    }

    public void reassignControllersForProcesses(List<Process> processes) {
        try (var executor = Executors.newSingleThreadScheduledExecutor()) {
            multiControllerService.unassignAllToons();
            executor.schedule(
                    () -> {
                        assignControllerToWindows(processes);
                    },
                    100,
                    TimeUnit.MILLISECONDS);
        }
    }

    public abstract void onProcessEnd(Process process);
}
