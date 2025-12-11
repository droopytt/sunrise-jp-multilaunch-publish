package com.malt.multilaunch.window;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.model.Config;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.multicontroller.WindowAssignRequest;
import com.malt.multilaunch.ui.ActiveAccountManager;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface WindowService {
    Logger LOG = LoggerFactory.getLogger(WindowService.class);
    int MAX_REASONABLE_OFFSET = 30;

    double MULTIPLIER = 0.35f;

    static WindowService create() {
        return new DefaultWindowService();
    }

    List<Rectangle> createTargetWindowRects(Rectangle workingArea, int numWindows, int offset);

    List<Rectangle> createDualOnlyRects(Rectangle workingArea, int offset);

    List<Rectangle> groupIntoFours(int numWindows, List<Rectangle> windowRects);

    void resizeWindowsForAccounts(List<Account> accounts, ActiveAccountManager activeAccountManager, Config config);

    void resizeWindowsWithRectangles(
            List<Account> openAccounts, ActiveAccountManager activeAccountManager, List<Rectangle> list, Config config);

    void assignControllerToWindows(List<Process> processes, MultiControllerService multiControllerService);

    class DefaultWindowService implements WindowService {

        private static void resize(
                List<Account> accounts,
                ActiveAccountManager activeAccountManager,
                List<Rectangle> rectangles,
                Config config) {
            if (accounts.size() < 2 && !config.stickySessions()) {
                return;
            }
            if (rectangles.size() < accounts.size()) {
                LOG.warn(
                        "Had {} rectangles but {} accounts than processes to resize, returning.",
                        rectangles.size(),
                        accounts.size());
            } else {
                for (int i = 0; i < accounts.size(); i++) {
                    var account = accounts.get(i);
                    var next = config.stickySessions()
                            ? activeAccountManager.findWindowRect(account).orElse(rectangles.get(i))
                            : rectangles.get(i);
                    CompletableFuture.runAsync(() -> {
                        var process = activeAccountManager
                                .findProcessForAccount(account)
                                .orElseThrow();
                        var hwnd = WindowUtils.getWindowHandleForProcessId((int) process.pid());
                        WindowUtils.resize(hwnd, next.x, next.y, next.width, next.height);
                        activeAccountManager.putWindow(account, next);
                    });
                }
            }
        }

        private Dimension getOffsets(Process process) {
            var hwnd = WindowUtils.getWindowHandleForProcessId(process.pid());
            var windowRect = WindowUtils.getWindowRect(hwnd);
            var clientRect = WindowUtils.getClientRect(hwnd);
            int borderWidth = (windowRect.right - windowRect.left) - clientRect.right;
            int borderHeight = (windowRect.bottom - windowRect.top) - clientRect.bottom;
            return new Dimension(borderWidth, borderHeight);
        }

        @Override
        public List<Rectangle> createTargetWindowRects(Rectangle workingArea, int numWindows, int offset) {
            var windowRects = new ArrayList<Rectangle>();

            if (numWindows == 2) {
                return createDualOnlyRects(workingArea, offset);
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

            return groupIntoFours(numWindows, windowRects);
        }

        @Override
        public List<Rectangle> createDualOnlyRects(Rectangle workingArea, int offset) {
            var windowData = WindowData.create(workingArea, 4, offset);
            var extraHeight = (int) (windowData.height() * (MULTIPLIER));
            var left = new Rectangle(
                    windowData.startX(), windowData.startY(), windowData.width(), windowData.height() + extraHeight);
            var right = new Rectangle(
                    windowData.startX() + windowData.incrementX(),
                    windowData.startY(),
                    windowData.width(),
                    windowData.height() + extraHeight);
            LOG.trace("Window data for toons was {}, offset was {}", List.of(left, right), offset);
            return List.of(left, right);
        }

        @Override
        public List<Rectangle> groupIntoFours(int numWindows, List<Rectangle> windowRects) {
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
                addToPartition(partitions.get(i % numPartitions), groupedRect);
            }

            return partitions.stream().flatMap(List::stream).toList();
        }

        private void addToPartition(List<Rectangle> partition, List<Rectangle> group) {
            partition.addAll(group);
        }

        @Override
        public void resizeWindowsForAccounts(
                List<Account> accounts, ActiveAccountManager activeAccountManager, Config config) {
            var physicalSize = DPIUtils.getPhysicalScreenSize();

            double scalingFactor = DPIUtils.getPrimaryMonitorScalingFactor();
            LOG.debug(
                    "Using scaling factor: {} for screen size: {}x{}",
                    scalingFactor,
                    physicalSize.width,
                    physicalSize.height);

            var insets = Toolkit.getDefaultToolkit()
                    .getScreenInsets(GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice()
                            .getDefaultConfiguration());

            var workingArea = new Rectangle(
                    insets.left,
                    insets.top,
                    physicalSize.width - insets.left - insets.right,
                    physicalSize.height - insets.top - insets.bottom);

            var testAccount = accounts.stream()
                    .map(activeAccountManager::findProcessForAccount)
                    .flatMap(Optional::stream)
                    .findAny()
                    .orElseThrow();
            var offset = getOffsets(testAccount).width;

            if (offset > MAX_REASONABLE_OFFSET) {
                LOG.debug("Offset was suspiciously large ({}), using default value of 16", offset);
                offset = 16;
            }

            List<Rectangle> rectangles;
            if (config.stickySessions()) {
                var sessionAccounts = new ArrayList<>(activeAccountManager.activeSession()).stream().sorted(Comparator.comparingInt(accounts::indexOf)).toList();
                rectangles = createTargetWindowRects(workingArea, sessionAccounts.size(), offset);

                var accountRectangles = new ArrayList<Rectangle>();
                for (var account : accounts) {
                    var existingRect = activeAccountManager.findWindowRect(account);
                    if (existingRect.isPresent()) {
                        accountRectangles.add(existingRect.get());
                    } else {
                        int sessionIndex = sessionAccounts.indexOf(account);
                        if (sessionIndex >= 0 && sessionIndex < rectangles.size()) {
                            accountRectangles.add(rectangles.get(sessionIndex));
                        }
                    }
                }
                rectangles = accountRectangles;
            } else {
                LOG.debug("Accounts are {}", accounts);
                rectangles = createTargetWindowRects(workingArea, accounts.size(), offset);
            }

            resizeWindowsWithRectangles(accounts, activeAccountManager, rectangles, config);
        }

        @Override
        public void resizeWindowsWithRectangles(
                List<Account> accounts,
                ActiveAccountManager activeAccountManager,
                List<Rectangle> rectangles,
                Config config) {
            resize(accounts, activeAccountManager, rectangles, config);
        }

        @Override
        // TODO return the assigned windows
        public void assignControllerToWindows(List<Process> processes, MultiControllerService multiControllerService) {
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
    }
}
