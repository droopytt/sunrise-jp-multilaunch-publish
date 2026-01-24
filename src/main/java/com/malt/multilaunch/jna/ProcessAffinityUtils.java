package com.malt.multilaunch.jna;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessAffinityUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessAffinityUtils.class);

    private static final int PROCESS_SET_INFORMATION = 0x0200;
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.DEFAULT_OPTIONS);

        HANDLE OpenProcess(int dwDesiredAccess, boolean bInheritHandle, int dwProcessId);

        boolean SetProcessAffinityMask(HANDLE hProcess, long dwProcessAffinityMask);

        boolean CloseHandle(HANDLE hObject);
    }

    public static void setAffinity(long pid, int coreIndex) {
        long mask = 1L << coreIndex;

        HANDLE handle =
                Kernel32.INSTANCE.OpenProcess(PROCESS_SET_INFORMATION | PROCESS_QUERY_INFORMATION, false, (int) pid);

        if (handle == null) {
            throw new RuntimeException("Failed to open process: " + pid);
        }

        try {
            boolean success = Kernel32.INSTANCE.SetProcessAffinityMask(handle, mask);
            if (!success) {
                throw new RuntimeException("SetProcessAffinityMask failed for PID " + pid);
            }

            LOG.debug("Process {} pinned to core {}", pid, coreIndex);
        } finally {
            Kernel32.INSTANCE.CloseHandle(handle);
        }
    }
}
