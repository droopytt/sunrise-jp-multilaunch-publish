package com.malt.multilaunch.window;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DPIUtils {
    private static final Logger LOG = LoggerFactory.getLogger(DPIUtils.class);

    private interface User32DPI extends StdCallLibrary {
        User32DPI INSTANCE = Native.load("user32", User32DPI.class);

        boolean SetProcessDPIAware();

        int GetDpiForWindow(HWND hwnd);

        int GetSystemMetrics(int nIndex);
    }

    private interface Shcore extends StdCallLibrary {
        Shcore INSTANCE = Native.load("Shcore", Shcore.class);

        int SetProcessDpiAwareness(int value);

        int GetDpiForMonitor(
                com.sun.jna.Pointer hmonitor,
                int dpiType,
                com.sun.jna.ptr.IntByReference dpiX,
                com.sun.jna.ptr.IntByReference dpiY);
    }

    // DPI Awareness values
    private static final int PROCESS_DPI_UNAWARE = 0;
    private static final int PROCESS_SYSTEM_DPI_AWARE = 1;
    private static final int PROCESS_PER_MONITOR_DPI_AWARE = 2;

    // System metrics indices
    private static final int SM_CXSCREEN = 0;
    private static final int SM_CYSCREEN = 1;

    private static volatile boolean dpiAwarenessSet = false;
    private static volatile double scalingFactor = 1.0;

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

    /**
     * Gets the actual physical screen dimensions in pixels.
     */
    public static java.awt.Dimension getPhysicalScreenSize() {
        try {
            int width = User32DPI.INSTANCE.GetSystemMetrics(SM_CXSCREEN);
            int height = User32DPI.INSTANCE.GetSystemMetrics(SM_CYSCREEN);
            LOG.debug("Physical screen size: {}x{}", width, height);
            return new java.awt.Dimension(width, height);
        } catch (Exception e) {
            LOG.error("Failed to get physical screen size", e);
            // Fallback to Java's method
            java.awt.GraphicsDevice gd =
                    java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            return new java.awt.Dimension(
                    gd.getDisplayMode().getWidth(), gd.getDisplayMode().getHeight());
        }
    }

    /**
     * Calculates the DPI scaling factor for the given window.
     */
    public static double getScalingFactor(HWND hwnd) {
        try {
            int dpi = User32DPI.INSTANCE.GetDpiForWindow(hwnd);
            double factor = dpi / 96.0; // 96 is the standard DPI
            LOG.debug("DPI for window: {}, scaling factor: {}", dpi, factor);
            return factor;
        } catch (Exception e) {
            LOG.debug("Could not get DPI for window, using default scaling", e);
            return 1.0;
        }
    }

    /**
     * Gets the primary monitor's DPI scaling factor.
     */
    public static double getPrimaryMonitorScalingFactor() {
        try {
            // Get DPI of primary monitor
            HWND hwnd = User32.INSTANCE.GetDesktopWindow();
            return getScalingFactor(hwnd);
        } catch (Exception e) {
            LOG.debug("Could not get primary monitor scaling factor", e);
            return 1.0;
        }
    }

    /**
     * Converts scaled coordinates to physical pixels.
     */
    public static int toPhysicalPixels(int scaledValue, double scalingFactor) {
        return (int) Math.round(scaledValue * scalingFactor);
    }

    /**
     * Converts physical pixels to scaled coordinates.
     */
    public static int toScaledPixels(int physicalValue, double scalingFactor) {
        return (int) Math.round(physicalValue / scalingFactor);
    }
}
