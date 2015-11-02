package com.dmh.tpf;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.file.InvalidPathException;
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

        User32.INPUT fakeInput          = new User32.INPUT();
        fakeInput.type                  = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        fakeInput.input.ki.wScan        = new WinDef.WORD(0);
        fakeInput.input.ki.wVk          = new WinDef.WORD(keyCode);
        fakeInput.input.ki.dwFlags      = new WinDef.DWORD(0);
        fakeInput.input.ki.dwExtraInfo  = new BaseTSD.ULONG_PTR(0);

        fakeInput.input.setType(WinUser.KEYBDINPUT.class);

        WinUser.INPUT[] inputs = {fakeInput};
        int cbSize = fakeInput.size();
        WinDef.DWORD result;
        WinDef.DWORD dwOne = new WinDef.DWORD(1);

        // this is required, trying to fake by sending WM_KEYDOWN msgs etc doesnt work for shit
        // damn gross win8 thing. if it already has focus and you call this, it LOSES input focus while flashing
        WinDef.HWND foregroundHwnd = User32.INSTANCE.GetForegroundWindow();
        if (foregroundHwnd.getPointer() != handle.getPointer()) {
            // bizzare workaround: send ALT first
            //fakeInput.input.ki.wVk          = new WinDef.WORD(0x12);
            // keydown is default
            //result = User32.INSTANCE.SendInput(dwOne, inputs, cbSize);

            //try {
            //    Thread.sleep(50);
            //} catch (InterruptedException e) {


            //fakeInput.input.ki.dwFlags = new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);
            //result = User32.INSTANCE.SendInput(dwOne, inputs, cbSize);

            //fakeInput.input.ki.wVk          = new WinDef.WORD(keyCode);

            // this has serious limitations: https://msdn.microsoft.com/en-us/library/windows/desktop/ms633539(v=vs.85).aspx
            // basically, need to start process ourselves to be able to do this. may switch to fullpath and do so
            User32.INSTANCE.SetForegroundWindow(handle);
            //User32.INSTANCE.SetFocus(handle);
            try {
                Thread.sleep(125);
            } catch (InterruptedException e) {
            }
        }


        result = User32.INSTANCE.SendInput(dwOne, inputs, cbSize);
        if (result.intValue() != 1)
            return false;

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        // send key up right after simulating tapping an input
        fakeInput.input.ki.dwFlags = new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);
        result = User32.INSTANCE.SendInput(dwOne, inputs, cbSize);
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
            //System.out.printf("Trying to create path '%s'\n", pathStr);
            // finally figured out what is blowing up. for some reason GetModuleFileNameExA() returns
            // "?" as a path for something. maybe a failed wide conversion or something
            try {
                if (pathStr.length() < 2)
                    return true;

                Path procPath = Paths.get(pathStr);
                String fileName = procPath.getFileName().toString();

                if (fileName.equalsIgnoreCase(processName)) {
                    this.handle = hWnd;
                    return false;
                }
            } catch (InvalidPathException ex) {
                return true;
            }

            // keep searching
            return true;
        }, null);
        return this.handle != null;
    }
}
