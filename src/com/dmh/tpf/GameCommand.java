package com.dmh.tpf;

public class GameCommand {
    private int     keycode;    // w32 keycode
    private String  chatString;
    private float   queueRate;  // in seconds

    public GameCommand() {
        queueRate = 1f;
        chatString = "";
    }

    public GameCommand(String chatStr, int keycode, float queueRate) {
        this.chatString = chatStr;
        this.keycode = keycode;
        this.queueRate = queueRate;
    }

    public float getQueueRate() {
        return queueRate;
    }

    public void setQueueRate(float queueRate) {
        this.queueRate = queueRate;
    }

    public int getKeycode() {
        return keycode;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public String getChatString() {
        return chatString;
    }

    public void setChatString(String chatString) {
        this.chatString = chatString;
    }

    @Override
    public String toString() {
        return String.format("%s,%d,%.2f", chatString, keycode, queueRate);
    }

    public static GameCommand fromString(String str) {
        GameCommand ret = new GameCommand();
        if (str == null || str.length() < 3)
            return ret;

        String[] chunks = str.split(",");
        if (chunks.length != 3)
            return ret;

        ret.chatString = chunks[0];
        
        try {
           ret.keycode = Integer.parseInt(chunks[1]);
        } catch (NumberFormatException ex) {
            // log me
        }

        try {
            ret.queueRate = Float.parseFloat(chunks[2]);
        } catch (NumberFormatException ex) {
            // log me
        }
        return ret;
    }
}
