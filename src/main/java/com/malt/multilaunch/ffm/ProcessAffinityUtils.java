package com.malt.multilaunch.ffm;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessAffinityUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ProcessAffinityUtils.class);
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup KERNEL32 = SymbolLookup.libraryLookup("kernel32", Arena.global());

    private static final MethodHandle OpenProcess = LINKER.downcallHandle(
            KERNEL32.find("OpenProcess").orElseThrow(), FunctionDescriptor.of(ADDRESS, JAVA_INT, JAVA_INT, JAVA_INT));

    private static final MethodHandle SetProcessAffinityMask = LINKER.downcallHandle(
            KERNEL32.find("SetProcessAffinityMask").orElseThrow(), FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_LONG));

    private static final MethodHandle CloseHandle =
            LINKER.downcallHandle(KERNEL32.find("CloseHandle").orElseThrow(), FunctionDescriptor.of(JAVA_INT, ADDRESS));

    private static final int PROCESS_SET_INFORMATION = 0x0200;
    private static final int PROCESS_QUERY_INFORMATION = 0x0400;

    public static void setAffinity(long pid, int coreIndex) {
        try (var arena = Arena.ofConfined()) {
            long mask = 1L << coreIndex;

            var handle = (MemorySegment)
                    OpenProcess.invoke(PROCESS_SET_INFORMATION | PROCESS_QUERY_INFORMATION, 0, (int) pid);

            if (handle.address() == MemorySegment.NULL.address()) {
                throw new RuntimeException("Failed to open process: " + pid);
            }

            int ok = (int) SetProcessAffinityMask.invoke(handle, mask);
            if (ok == 0) {
                throw new RuntimeException("SetProcessAffinityMask failed for PID " + pid);
            }

            CloseHandle.invoke(handle);
            LOG.debug("Process {} pinned to core {}", pid, coreIndex);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
