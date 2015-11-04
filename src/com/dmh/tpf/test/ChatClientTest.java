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

package com.dmh.tpf.test;

import com.dmh.tpf.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChatClientTest {

    private final String nick = "nick";
    private final String oauth = "oauth:";
    private final String chan = "#chan";

    @Test
    public void ConnectTest(){
        ChatClient cc = new ChatClient(nick, oauth, chan);
        assertTrue(cc.connect());
        assertTrue(cc.disconnect());
    }

    @Test
    public void LoginTest() {

        ChatClient cc = new ChatClient(nick, oauth, chan);
        final boolean[] gotGoodAuth = {false};

        cc.addListener(rawLine -> {
            IrcMessage msg = ChatParser.parse(rawLine);
            // 004 = we connected, pass
            if (msg.getType() != MessageType.Numeric)
                return;
            String cmd = msg.getCommand();
            if (cmd != null && cmd.equalsIgnoreCase("004")) {
                gotGoodAuth[0] = true;
                cc.disconnect();
            }
        });

        assertTrue(cc.connect());

        cc.sendAuth();

        cc.readLoopThread();
        try {
            Thread.sleep(3000);
            assertTrue(gotGoodAuth[0] == true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        finally {
            cc.disconnect();
        }
    }

    // note: not going too crazy on irc tests here

    @Test
    public void JoinChannelTest() {

        ChatClient cc = new ChatClient(nick, oauth, chan);
        final boolean[] gotJoinMsg = {false};

        cc.addListener(rawLine -> {
            IrcMessage msg = ChatParser.parse(rawLine);
            // 004 = we connected, pass
            if (msg.getType() != MessageType.Numeric)
                return;
            String cmd = msg.getCommand();
            if (cmd == null)
                return;

            if (cmd.equalsIgnoreCase("004"))
                cc.joinChannel(chan);
            else if (cmd.equalsIgnoreCase("353")) {
                // this is NAMES. assume it was the ONE chan we treid to join..
                gotJoinMsg[0] = true;
                cc.disconnect();
            }
        });

        assertTrue(cc.connect());

        cc.sendAuth();

        cc.readLoopThread();
        try {
            Thread.sleep(3000);
            assertTrue(gotJoinMsg[0] == true);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue(false);
        }
        finally {
            cc.disconnect();
        }
    }
}