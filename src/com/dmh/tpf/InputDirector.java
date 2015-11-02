package com.dmh.tpf;

public abstract class InputDirector {
    protected String processName;
    public abstract boolean sendKey(int keyCode);
    //public abstract boolean sendMouse(int x, int y, boolean isDown);
    public abstract boolean findInstance();

    protected InputDirector(String processName) {
        this.processName = processName;
    }
}
