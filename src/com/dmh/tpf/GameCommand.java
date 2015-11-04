/*
twitch-plays-framework Copyright 2015 Dexter Haslem <dexter.haslem@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.dmh.tpf;

public class GameCommand {
    private int     keycode;    // w32 keycode
    private String  chatString;
    //private float   queueRate;  // in seconds

    public GameCommand() {
        //queueRate = 1f;
        chatString = "";
    }

    public GameCommand(String chatStr, int keycode/*, float queueRate*/) {
        this.chatString = chatStr;
        this.keycode = keycode;
        //this.queueRate = queueRate;
    }

//    public float getQueueRate() {
//        return queueRate;
//    }
//
//    public void setQueueRate(float queueRate) {
//        this.queueRate = queueRate;
//    }

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
        return String.format("%s,%d"/*,%.2f"*/, chatString, keycode);//, queueRate);
    }

    public static GameCommand fromString(String str) {
        GameCommand ret = new GameCommand();
        if (str == null || str.length() < 3)
            return ret;

        String[] chunks = str.split(",");
        if (chunks.length != 2)// 3)
            return ret;

        ret.chatString = chunks[0].trim();
        
        try {
           ret.keycode = Integer.parseInt(chunks[1].trim());
        } catch (NumberFormatException ex) {
            // log me
        }

//        try {
//            ret.queueRate = Float.parseFloat(chunks[2]);
//        } catch (NumberFormatException ex) {
//            // log me
//        }
        return ret;
    }
}
