package com.malt.multilaunch.window;

import com.malt.multilaunch.model.Account;
import com.malt.multilaunch.multicontroller.MultiControllerService;
import com.malt.multilaunch.multicontroller.WindowAssignRequest;
import com.malt.multilaunch.ui.ActiveAccountManager;
import java.awt.*;
import java.util.ArrayList;
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

    void resizeWindowsForProcesses(List<Account> accounts, ActiveAccountManager activeAccountManager);

    void resizeWindowsWithRectangles(
            List<Account> openAccounts, ActiveAccountManager activeAccountManager, List<Rectangle> list);

    void assignControllerToWindows(List<Process> processes, MultiControllerService multiControllerService);

    class DefaultWindowService implements WindowService {

        private static void resize(
                List<Account> accounts, ActiveAccountManager activeAccountManager, List<Rectangle> rectangles) {
            if (accounts.size() < 2) {
                return;
            }
            if (rectangles.size() < accounts.size()) {
                LOG.warn(
                        "Had {} rectangles but {} accounts than processes to resize, returning.",
                        rectangles.size(),
                        accounts.size());
            } else {
                for (int i = 0; i < accounts.size(); i++) {
                    var finalI = i;
                    CompletableFuture.runAsync(() -> {
                        var account = accounts.get(finalI);
                        var process = activeAccountManager
                                .findProcessForAccount(account)
                                .orElseThrow();
                        var hwnd = WindowUtils.getWindowHandleForProcessId((int) process.pid());
                        var oldWindowRect = WindowUtils.getWindowRect(hwnd);
                        var next = rectangles.get(finalI);
                        WindowUtils.resize(hwnd, next.x, next.y, next.width, next.height);
                        activeAccountManager.putWindow(account, next);
                        var newWindowRect = WindowUtils.getWindowRect(hwnd);
                        LOG.trace(
                                "Window rect for pid {} was {} and is now {}",
                                process.pid(),
                                oldWindowRect,
                                newWindowRect);
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
        public void resizeWindowsForProcesses(List<Account> accounts, ActiveAccountManager activeAccountManager) {
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
            var offset = getOffsets(testAccount).width;

            if (offset > MAX_REASONABLE_OFFSET) {
                LOG.debug("Offset was suspiciously large ({}), using default value of 16", offset);
                offset = 16;
            }

            var rectangles = createTargetWindowRects(workingArea, accounts.size(), offset);

            resizeWindowsWithRectangles(accounts, activeAccountManager, rectangles);
        }

        @Override
        public void resizeWindowsWithRectangles(
                List<Account> accounts, ActiveAccountManager activeAccountManager, List<Rectangle> rectangles) {
            resize(accounts, activeAccountManager, rectangles);
        }

        @Override
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
