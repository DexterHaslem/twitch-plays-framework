package com.dmh.tpf;

public class Main {
    public static void main(String[] args) {
        InputDirector cd = new InputDirector("notepad.exe");
        if (cd.findRunningHandle()) {
            //https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
            cd.sendVirtualKeyCode(0x46);
            cd.sendVirtualKeyCode(0x41);
            cd.sendVirtualKeyCode(0x52);
            cd.sendVirtualKeyCode(0x54);
        }
    }
}
