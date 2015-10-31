package com.dmh.tpf;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.W32APIOptions;
import com.sun.jna.Native;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Dexter on 10/31/2015.
 */
public class CommandDirector {
    //
    private String processName;
    // live process handle (if found)
    //private WinNT.HANDLE processHandle;
    private WinNT.HWND hwnd;

    private Kernel32 kernel32;

    public CommandDirector(String processName) {
        this.processName = processName;
        kernel32 = (Kernel32) Native.loadLibrary(Kernel32.class, W32APIOptions.UNICODE_OPTIONS);
    }

    //public void closeHandle() {
    //    if (processHandle != null)
   //         kernel32.CloseHandle(processHandle);
   // }

    public void sendVirtualKeyCode(int keyCode){
        //final int WM_CHAR = 0x0102;
        try {
            User32.INSTANCE.SetForegroundWindow(hwnd);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        User32.INPUT fakeInput = new User32.INPUT();
        fakeInput.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
        fakeInput.input.setType(WinUser.KEYBDINPUT.class);
        fakeInput.input.ki.wScan = new WinDef.WORD(0);
        fakeInput.input.ki.wVk = new WinDef.WORD(keyCode);
        fakeInput.input.ki.dwFlags = new WinDef.DWORD(0);
        fakeInput.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        WinUser.INPUT[] inputs =  { fakeInput };
        int cbSize = fakeInput.size();
        // keydown
        WinDef.DWORD result = User32.INSTANCE.SendInput(new WinDef.DWORD(1), inputs, cbSize);

        // send key up
        fakeInput.input.ki.dwFlags = new WinDef.DWORD(WinUser.KEYBDINPUT.KEYEVENTF_KEYUP);
        result = User32.INSTANCE.SendInput(new WinDef.DWORD(1), inputs, cbSize);
    }
/*
    public boolean openRunningHandle() {
        Tlhelp32.PROCESSENTRY32.ByReference processEntry = new Tlhelp32.PROCESSENTRY32.ByReference();
        WinNT.HANDLE snapshot = kernel32.CreateToolhelp32Snapshot(Tlhelp32.TH32CS_SNAPPROCESS, new WinDef.DWORD(0));

        try  {
            while (kernel32.Process32Next(snapshot, processEntry)) {
                String curProcName = Native.toString(processEntry.szExeFile);

                if (!curProcName.equalsIgnoreCase(processName))
                    continue;

                closeHandle();
                // getting handle from process id is a pain in the ass
                processHandle = kernel32.OpenProcess(0, false, processEntry.th32ProcessID.intValue());
            }
        }
        finally {
            kernel32.CloseHandle(snapshot);
        }

        return true;
    }*/

    public boolean findRunningHandle() {
        final User32 user32 = User32.INSTANCE;
        user32.EnumWindows((hWnd, arg1) -> {
            byte[] path = new byte[1024];
            //user32.GetWindowModuleFileName(

            // lord, this doesnt work for shit
            // user32.GetWindowModuleFileName(hWnd, path, 1024);


            IntByReference pointer = new IntByReference();
            user32.GetWindowThreadProcessId(hWnd, pointer);
            WinNT.HANDLE procHandle = kernel32.OpenProcess(0x0400 | 0x0010, false, pointer.getValue());

            Psapi.INSTANCE.GetModuleFileNameExA(procHandle, null, path, 1024);

            String pathStr = Native.toString(path);
            // its a full path, blah convert to just filename
            Path procPath  = Paths.get(pathStr);
            String fileName = procPath.getFileName().toString();

            System.out.printf("fullpath= %s filename=(%s)\n", pathStr, fileName);

            if (fileName.equalsIgnoreCase(processName)) {
                this.hwnd = hWnd;
                return false;
            }

            // keep searching
            return true;
        }, null);
        return this.hwnd != null;
    }
}
