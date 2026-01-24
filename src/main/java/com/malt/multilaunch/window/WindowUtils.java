package com.malt.multilaunch.window;

import com.malt.multilaunch.ui.UltiLauncher;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.LPARAM;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public final class WindowUtils {

    public static void sendCloseSignal(Process process) {
        int targetPid = (int) process.pid();

        User32Ext.INSTANCE.EnumWindows(
                (hWnd, data) -> {
                    IntByReference pidRef = new IntByReference();
                    User32Ext.INSTANCE.GetWindowThreadProcessId(hWnd, pidRef);

                    if (pidRef.getValue() == targetPid) {
                        User32Ext.INSTANCE.PostMessageW(hWnd, User32Ext.WM_CLOSE, new WPARAM(0), new LPARAM(0));
                    }
                    return true;
                },
                null);
    }

    private interface User32Ext extends StdCallLibrary {
        int WM_CLOSE = 0x0010;

        boolean PostMessageW(HWND hWnd, int msg, WPARAM wParam, LPARAM lParam);

        User32Ext INSTANCE = Native.load("user32", User32Ext.class);

        boolean GetWindowRect(HWND hWnd, RECT rect);

        boolean GetClientRect(HWND hWnd, RECT rect);

        boolean SetWindowPos(HWND hWnd, HWND hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);

        HWND GetForegroundWindow();

        int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);

        boolean EnumWindows(com.sun.jna.platform.win32.WinUser.WNDENUMPROC callback, Pointer data);
    }

    private WindowUtils() {}

    public static RECT getWindowRect(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        var rect = new RECT();
        if (User32Ext.INSTANCE.GetWindowRect(hWnd, rect)) {
            return rect;
        }
        return new RECT();
    }

    public static boolean isToontownWindowActive() {
        var hwnd = User32Ext.INSTANCE.GetForegroundWindow();
        var windowTitle = getWindowTitle(hwnd);
        return windowTitle.contains("Toontown") || windowTitle.equals(UltiLauncher.WINDOW_TITLE);
    }

    public static String getWindowTitle(HWND hwnd) {
        var buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer);
    }

    public static RECT getClientRect(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        var rect = new RECT();
        if (User32Ext.INSTANCE.GetClientRect(hWnd, rect)) {
            return rect;
        }
        return new RECT();
    }

    public static void resize(long hwnd, int x, int y, int width, int height) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        User32Ext.INSTANCE.SetWindowPos(hWnd, null, x, y, width, height, 0);
    }

    public static long getWindowHandleForProcessId(long pid) {
        long[] result = {0};
        User32Ext.INSTANCE.EnumWindows(
                (hwnd, data) -> {
                    IntByReference processId = new IntByReference();
                    User32Ext.INSTANCE.GetWindowThreadProcessId(hwnd, processId);
                    if (processId.getValue() == pid) {
                        result[0] = Pointer.nativeValue(hwnd.getPointer());
                        return false;
                    }
                    return true;
                },
                null);
        return result[0];
    }

    public static boolean isWindowReady(long pid) {
        var hwnd = getWindowHandleForProcessId(pid);
        if (hwnd == 0) {
            return false;
        }

        HWND handle = new HWND(new Pointer(hwnd));
        return User32.INSTANCE.IsWindowVisible(handle) && isWindowRendered(handle);
    }

    private static boolean isWindowRendered(HWND hwnd) {
        WinDef.RECT rect = new WinDef.RECT();
        if (!User32.INSTANCE.GetWindowRect(hwnd, rect)) {
            return false;
        }

        int width = rect.right - rect.left;
        int height = rect.bottom - rect.top;

        return width > 0 && height > 0;
    }
}
