package com.malt.multilaunch.ffm;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.PointerByReference;

public class ProcessVolumeMuter {

    private static final int CLSCTX_ALL = 0x17;

    private static final Guid.CLSID CLSID_MMDeviceEnumerator = new Guid.CLSID("BCDE0395-E52F-467C-8E3D-C4579291692E");

    private static final Guid.GUID IID_IMMDeviceEnumerator = new Guid.GUID("A95664D2-9614-4F35-A746-DE8DB63617E6");

    private static final Guid.GUID IID_IAudioSessionManager2 = new Guid.GUID("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F");

    private static final Guid.REFIID IID_IAudioSessionControl2 =
            new Guid.REFIID(new Guid.IID("bfb7ff88-7239-4fc9-8fa2-07c950be9c6d"));

    private static final Guid.REFIID IID_ISimpleAudioVolume =
            new Guid.REFIID(new Guid.IID("87CE5498-68D6-44E5-9215-6DA47EF883D8"));

    private static final ThreadLocal<Boolean> comInitialized = ThreadLocal.withInitial(() -> false);

    private static void ensureComInitialized() {
        if (!comInitialized.get()) {
            var hr = Ole32.INSTANCE.CoInitializeEx(Pointer.NULL, Ole32.COINIT_MULTITHREADED);
            if (!COMUtils.FAILED(hr) || hr.intValue() == 1) {
                comInitialized.set(true);
            }
        }
    }

    public static boolean muteProcess(long processId) {
        return setProcessVolume(processId, 0.0f, true);
    }

    public static boolean setProcessVolume(long processId, float volume, boolean mute) {
        ensureComInitialized();

        try {
            var ppv = new PointerByReference();
            var hr = Ole32.INSTANCE.CoCreateInstance(
                    CLSID_MMDeviceEnumerator, null, CLSCTX_ALL, IID_IMMDeviceEnumerator, ppv);

            if (COMUtils.FAILED(hr)) return false;

            var deviceEnum = new IMMDeviceEnumerator(ppv.getValue());
            var devicePtr = new PointerByReference();
            hr = deviceEnum.GetDefaultAudioEndpoint(0, 0, devicePtr);

            if (COMUtils.FAILED(hr)) {
                deviceEnum.Release();
                return false;
            }

            var device = new IMMDevice(devicePtr.getValue());
            var sessionMgrPtr = new PointerByReference();
            hr = device.Activate(IID_IAudioSessionManager2, CLSCTX_ALL, null, sessionMgrPtr);

            if (COMUtils.FAILED(hr)) {
                device.Release();
                deviceEnum.Release();
                return false;
            }

            var sessionMgr = new IAudioSessionManager2(sessionMgrPtr.getValue());
            var enumPtr = new PointerByReference();
            hr = sessionMgr.GetSessionEnumerator(enumPtr);

            if (COMUtils.FAILED(hr)) {
                sessionMgr.Release();
                device.Release();
                deviceEnum.Release();
                return false;
            }

            var sessionEnum = new IAudioSessionEnumerator(enumPtr.getValue());
            var count = new int[1];
            sessionEnum.GetCount(count);

            var found = false;

            for (var i = 0; i < count[0]; i++) {
                IAudioSessionControl control = null;
                ISimpleAudioVolume volumeControl = null;

                try {
                    var controlPtr = new PointerByReference();
                    sessionEnum.GetSession(i, controlPtr);

                    var baseControl = new IAudioSessionControl(controlPtr.getValue());
                    var control2Ptr = new PointerByReference();
                    hr = baseControl.QueryInterface(IID_IAudioSessionControl2, control2Ptr);
                    baseControl.Release();

                    if (COMUtils.FAILED(hr)) continue;

                    control = new IAudioSessionControl(control2Ptr.getValue());
                    var pidMem = new Memory(4); // DWORD is 4 bytes
                    hr = control.GetProcessId(pidMem);

                    if (COMUtils.FAILED(hr)) continue;

                    var pid = pidMem.getInt(0) & 0xFFFFFFFFL;

                    if (pid == processId) {
                        var volumePtr = new PointerByReference();
                        hr = control.QueryInterface(IID_ISimpleAudioVolume, volumePtr);

                        if (COMUtils.SUCCEEDED(hr)) {
                            volumeControl = new ISimpleAudioVolume(volumePtr.getValue());
                            volumeControl.SetMasterVolume(volume, Pointer.NULL);
                            volumeControl.SetMute(mute ? 1 : 0, Pointer.NULL);
                            found = true;
                        }
                    }
                } catch (Exception e) {
                } finally {
                    if (volumeControl != null)
                        try {
                            volumeControl.Release();
                        } catch (Exception e) {
                        }
                    if (control != null)
                        try {
                            control.Release();
                        } catch (Exception e) {
                        }
                }

                if (found) break;
            }

            sessionEnum.Release();
            sessionMgr.Release();
            device.Release();
            deviceEnum.Release();

            return found;

        } catch (Exception e) {
            return false;
        }
    }

    private static class IMMDeviceEnumerator extends Unknown {
        public IMMDeviceEnumerator(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT GetDefaultAudioEndpoint(int dataFlow, int role, PointerByReference ppDevice) {
            return (WinNT.HRESULT) _invokeNativeObject(
                    4, new Object[] {this.getPointer(), dataFlow, role, ppDevice}, WinNT.HRESULT.class);
        }
    }

    private static class IMMDevice extends Unknown {
        public IMMDevice(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT Activate(
                Guid.GUID iid, int dwClsCtx, Pointer pActivationParams, PointerByReference ppInterface) {
            return (WinNT.HRESULT) _invokeNativeObject(
                    3,
                    new Object[] {this.getPointer(), iid, dwClsCtx, pActivationParams, ppInterface},
                    WinNT.HRESULT.class);
        }
    }

    private static class IAudioSessionManager2 extends Unknown {
        public IAudioSessionManager2(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT GetSessionEnumerator(PointerByReference ppSessionEnum) {
            return (WinNT.HRESULT)
                    _invokeNativeObject(5, new Object[] {this.getPointer(), ppSessionEnum}, WinNT.HRESULT.class);
        }
    }

    private static class IAudioSessionEnumerator extends Unknown {
        public IAudioSessionEnumerator(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT GetCount(int[] pSessionCount) {
            return (WinNT.HRESULT)
                    _invokeNativeObject(3, new Object[] {this.getPointer(), pSessionCount}, WinNT.HRESULT.class);
        }

        public WinNT.HRESULT GetSession(int sessionIndex, PointerByReference ppSession) {
            return (WinNT.HRESULT) _invokeNativeObject(
                    4, new Object[] {this.getPointer(), sessionIndex, ppSession}, WinNT.HRESULT.class);
        }
    }

    private static class IAudioSessionControl extends Unknown {
        public IAudioSessionControl(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT GetProcessId(Pointer pRetVal) {
            return (WinNT.HRESULT)
                    _invokeNativeObject(14, new Object[] {this.getPointer(), pRetVal}, WinNT.HRESULT.class);
        }
    }

    private static class ISimpleAudioVolume extends Unknown {
        public ISimpleAudioVolume(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT SetMasterVolume(float fLevel, Pointer eventContext) {
            return (WinNT.HRESULT)
                    _invokeNativeObject(3, new Object[] {this.getPointer(), fLevel, eventContext}, WinNT.HRESULT.class);
        }

        public WinNT.HRESULT SetMute(int bMute, Pointer eventContext) {
            return (WinNT.HRESULT)
                    _invokeNativeObject(5, new Object[] {this.getPointer(), bMute, eventContext}, WinNT.HRESULT.class);
        }
    }
}
