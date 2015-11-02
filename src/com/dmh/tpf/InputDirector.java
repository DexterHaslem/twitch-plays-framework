package com.dmh.tpf;

import com.sun.corba.se.spi.orbutil.fsm.Input;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class InputDirector {
    protected String processName;
    public abstract boolean sendKey(int keyCode);
    //public abstract boolean sendMouse(int x, int y, boolean isDown);
    public abstract boolean findInstance();

    protected InputDirector(String processName) {
        this.processName = processName;
    }
}
