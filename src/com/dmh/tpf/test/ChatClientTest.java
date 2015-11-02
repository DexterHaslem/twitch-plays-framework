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