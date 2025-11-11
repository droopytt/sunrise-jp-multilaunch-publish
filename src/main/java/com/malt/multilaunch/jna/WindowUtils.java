package com.malt.multilaunch.jna;

import com.malt.multilaunch.ui.UltiLauncher;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.POINT;
import com.sun.jna.platform.win32.WinDef.RECT;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

public final class WindowUtils {

    private interface User32Ext extends StdCallLibrary {

        User32Ext INSTANCE = Native.load("user32", User32Ext.class);

        boolean GetWindowRect(HWND hWnd, RECT rect);

        boolean GetClientRect(HWND hWnd, RECT rect);

        boolean SetWindowPos(HWND hWnd, HWND hWndInsertAfter, int X, int Y, int cx, int cy, int uFlags);

        boolean SetForegroundWindow(HWND hWnd);

        HWND GetForegroundWindow();

        String GetWindowText();

        boolean IsWindow(HWND hWnd);

        boolean PostMessage(HWND hWnd, int msg, Pointer wParam, Pointer lParam);

        boolean IsHungAppWindow(HWND hWnd);

        boolean ShowWindow(HWND hWnd, int nCmdShow);

        boolean RedrawWindow(HWND hWnd, RECT lprcUpdate, WinNT.HANDLE hrgnUpdate, int flags);

        int GetWindowThreadProcessId(HWND hWnd, IntByReference lpdwProcessId);

        boolean EnumWindows(com.sun.jna.platform.win32.WinUser.WNDENUMPROC callback, Pointer data);

        HWND SetActiveWindow(HWND hWnd);

        void PostMessage(HWND hWnd, int Msg, WinDef.WPARAM wParam, WinDef.LPARAM lParam);
    }

    public interface User32Extra extends StdCallLibrary {
        User32Extra INSTANCE = Native.load("user32", User32Extra.class);

        boolean GetCursorPos(POINT point);

        HWND WindowFromPoint(POINT.ByValue point);

        HWND GetAncestor(HWND hwnd, int gaFlags);
    }

    private static final int WM_CLOSE = 0x0010;
    private static final int GA_ROOT = 2;
    private static final int WM_LBUTTONDOWN = 0x0201;
    private static final int WM_LBUTTONUP = 0x0202;
    private static final int SW_RESTORE = 9;

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
        var hwnd = User32.INSTANCE.GetForegroundWindow();
        var windowTitle = getWindowTitle(hwnd);
        return windowTitle.contains("Toontown") || windowTitle.equals(UltiLauncher.WINDOW_TITLE);
    }

    public static String getWindowTitle(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        return getWindowTitle(hWnd);
    }

    public static String getWindowTitle(HWND hwnd) {
        var buffer = new char[1024];
        User32.INSTANCE.GetWindowText(hwnd, buffer, buffer.length);
        return Native.toString(buffer);
    }

    public static RECT getClientRect(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        RECT rect = new RECT();
        if (User32Ext.INSTANCE.GetClientRect(hWnd, rect)) {
            return rect;
        }
        return new RECT();
    }

    public static void resize(long hwnd, int x, int y, int width, int height) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        User32Ext.INSTANCE.SetWindowPos(hWnd, null, x, y, width, height, 0);
    }

    public static void bringToFront(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        User32Ext.INSTANCE.SetForegroundWindow(hWnd);
    }

    public static void unminimize(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        User32Ext.INSTANCE.ShowWindow(hWnd, SW_RESTORE);
    }

    public static void sendClick(long hwnd, int x, int y) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        Pointer lParam = Pointer.createConstant((y << 16) | (x & 0xFFFF));
        User32Ext.INSTANCE.PostMessage(hWnd, WM_LBUTTONDOWN, Pointer.NULL, lParam);
        User32Ext.INSTANCE.PostMessage(hWnd, WM_LBUTTONUP, Pointer.NULL, lParam);
    }

    public static long getForegroundWindow() {
        var hwnd = User32Ext.INSTANCE.GetForegroundWindow();
        if (hwnd == null) {
            return 0;
        }
        return Pointer.nativeValue(hwnd.getPointer());
    }

    public static boolean closeWindow(long hwnd) {
        var hWnd = new HWND(Pointer.createConstant(hwnd));
        return User32Ext.INSTANCE.IsWindow(hWnd)
                && !User32Ext.INSTANCE.IsHungAppWindow(hWnd)
                && User32Ext.INSTANCE.PostMessage(hWnd, WM_CLOSE, Pointer.NULL, Pointer.NULL);
    }

    public static POINT getCursorPos() {
        POINT p = new POINT();
        User32Extra.INSTANCE.GetCursorPos(p);
        return p;
    }

    public static long getWindowFromPoint(POINT point) {
        HWND hwnd = User32Extra.INSTANCE.WindowFromPoint(new POINT.ByValue() {
            {
                x = point.x;
                y = point.y;
            }
        });

        if (hwnd == null) return 0;

        HWND topLevel = User32Extra.INSTANCE.GetAncestor(hwnd, GA_ROOT);
        if (topLevel == null) topLevel = hwnd;

        return Pointer.nativeValue(topLevel.getPointer());
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
}
