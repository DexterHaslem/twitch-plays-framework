package com.dmh.tpf;

public class Main {

    public static void main(String[] args) {
        CommandDirector cd = new CommandDirector("notepad.exe");
        if (cd.findRunningHandle()) {
            cd.sendVirtualKeyCode(0x41); // VK_A
        }
    }
}
