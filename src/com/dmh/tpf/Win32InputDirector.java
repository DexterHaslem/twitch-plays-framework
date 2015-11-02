package com.dmh.tpf;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Win32InputDirector extends InputDirector {
    // live process *window* handle (if found). its separate from process handle. luv win32
    private WinNT.HWND handle;

    private Kernel32 kernel32;

    public Win32InputDirector(String processName) {
        super(processName);
        kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
    }

    public boolean sendKey(int keyCode) {
        if (handle == null)
            return false;

        // this is required, trying to fake by sending WM_KEYDOWN msgs etc doesnt work for shit
        User32.INSTANCE.SetForegroundWindow(handle);

        User32.INPUT fakeInput          = new User32.INPUT();
        fakeInput.type                  = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        fakeInput.input.ki.wScan        = new WinDef.WORD(0);
        fakeInput.input.ki.wVk          = new WinDef.WORD(keyCode);
        fakeInput.input.ki.dwFlags      = new WinDef.DWORD(0);
        fakeInput.input.ki.dwExtraInfo  = new BaseTSD.ULONG_PTR(0);

        fakeInput.input.setType(WinUser.KEYBDINPUT.class);

        WinUser.INPUT[] inputs = {fakeInput};
        int cbSize = fakeInput.size();
        // keydown is default
        WinDef.DWORD result = User32.INSTANCE.SendInput(new WinDef.DWORD(1), inputs, cbSize);

        if (result.intValue() != 1)
            return false;

        // send key up right after simulating tapping an input
        fakeInput.input.ki.dwFlags = new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);
        result = User32.INSTANCE.SendInput(new WinDef.DWORD(1), inputs, cbSize);
        return result.intValue() == 1;
    }

    public boolean findInstance() {
        // caveat: assumes 1-window application (like most games)
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows((hWnd, param) -> {
            byte[] pathBytes = new byte[1024];

            // lord, this doesnt work for shit
            // user32.GetWindowModuleFileName(hWnd, path, 1024);

            IntByReference pointer = new IntByReference();
            user32.GetWindowThreadProcessId(hWnd, pointer);
            WinNT.HANDLE procHandle = kernel32.OpenProcess(0x0400 | 0x0010, false, pointer.getValue());

            Psapi.INSTANCE.GetModuleFileNameExA(procHandle, null, pathBytes, 1024);

            String pathStr = Native.toString(pathBytes);
            // its a full path, blah convert to just filename
            Path procPath = Paths.get(pathStr);
            String fileName = procPath.getFileName().toString();

            if (fileName.equalsIgnoreCase(processName)) {
                this.handle = hWnd;
                return false;
            }

            // keep searching
            return true;
        }, null);
        return this.handle != null;
    }
}
