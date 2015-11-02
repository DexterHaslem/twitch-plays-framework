package com.dmh.tpf;

public class Main {
    public static void main(String[] args) {
        InputDirector cd = new Win32InputDirector("notepad.exe");
        if (cd.findInstance()) {
            //https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
            cd.sendKey(0x46);
            cd.sendKey(0x41);
            cd.sendKey(0x52);
            cd.sendKey(0x54);
        }
    }
}
