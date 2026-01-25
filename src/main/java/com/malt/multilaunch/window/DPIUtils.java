package com.malt.multilaunch.window;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;

public final class DPIUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DPIUtils.class);

    private interface User32DPI extends StdCallLibrary {
        User32DPI INSTANCE = Native.load("user32", User32DPI.class);

        boolean SetProcessDPIAware();

        int GetSystemMetrics(int nIndex);
    }

    private interface Shcore extends StdCallLibrary {
        Shcore INSTANCE = Native.load("Shcore", Shcore.class);

        int SetProcessDpiAwareness(int value);

    }

    private static final int PROCESS_PER_MONITOR_DPI_AWARE = 2;

    private static final int SM_CXSCREEN = 0;
    private static final int SM_CYSCREEN = 1;

    private static volatile boolean dpiAwarenessSet = false;

    private DPIUtils() {}

    /**
     * Sets the process as DPI aware. Should be called early in application startup.
     */
    public static void setProcessDPIAware() {
        if (dpiAwarenessSet) {
            return;
        }

        try {
            // Try the newer API first (Windows 8.1+)
            int result = Shcore.INSTANCE.SetProcessDpiAwareness(PROCESS_PER_MONITOR_DPI_AWARE);
            if (result == 0) {
                LOG.info("Successfully set process as per-monitor DPI aware");
                dpiAwarenessSet = true;
                return;
            }
        } catch (Exception e) {
            LOG.debug("Shcore.SetProcessDpiAwareness not available, trying fallback", e);
        }

        try {
            // Fallback to older API (Windows Vista+)
            boolean result = User32DPI.INSTANCE.SetProcessDPIAware();
            if (result) {
                LOG.info("Successfully set process as system DPI aware");
                dpiAwarenessSet = true;
            } else {
                LOG.warn("Failed to set process DPI awareness");
            }
        } catch (Exception e) {
            LOG.error("Failed to set DPI awareness", e);
        }
    }

    public static java.awt.Dimension getPhysicalScreenSize() {
        try {
            int width = User32DPI.INSTANCE.GetSystemMetrics(SM_CXSCREEN);
            int height = User32DPI.INSTANCE.GetSystemMetrics(SM_CYSCREEN);
            LOG.debug("Physical screen size: {}x{}", width, height);
            return new java.awt.Dimension(width, height);
        } catch (Exception e) {
            LOG.error("Failed to get physical screen size", e);
            var gd =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            return new Dimension(
                    gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        }
    }

    public static double getPrimaryMonitorScalingFactor() {
        try {
            var gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice();
            var gc = gd.getDefaultConfiguration();
            var tx = gc.getDefaultTransform();
            double factor = tx.getScaleX();
            LOG.debug("Primary monitor scaling factor: {}", factor);
            return factor;
        } catch (Exception e) {
            LOG.debug("Could not get primary monitor scaling factor", e);
            return 1.0;
        }
    }

}
