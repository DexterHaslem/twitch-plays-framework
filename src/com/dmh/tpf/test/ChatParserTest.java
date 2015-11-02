package com.dmh.tpf.test;

import com.dmh.tpf.ChatParser;
import com.dmh.tpf.IrcMessage;
import com.dmh.tpf.MessageType;
import org.junit.Test;

import static org.junit.Assert.*;

public class ChatParserTest {
    
    @Test
    public void testParse() throws Exception {
        // numeric test
        String testNumeric = ":tmi.twitch.tv 004 twitch_username tmi.twitch.tv 0.0.1 w n";
        IrcMessage msg = ChatParser.parse(testNumeric);

        assertEquals(msg.getSource(), "tmi.twitch.tv");
        assertEquals(msg.getType(), MessageType.Numeric);
        assertEquals(msg.getParameters(), "twitch_username tmi.twitch.tv 0.0.1 w n");

        // privmsg test
        String testPrivMsg = ":foo!user@host PRIVMSG #channel :butt farts asdfadsf";
        msg = ChatParser.parse(testPrivMsg);

        assertEquals(msg.getType(), MessageType.Privmsg);
        assertEquals(msg.getPrivmsgMessage(), "butt farts asdfadsf");
        assertEquals(msg.getPrivmsgChannel(), "#channel");

        // PING test
        String pingTest = "PING :tmi.twitch.tv";
        msg = ChatParser.parse(pingTest);
        assertEquals(msg.getType(), MessageType.Ping);
        assertEquals(msg.getParameters(), "tmi.twitch.tv");

        // IRCv3 test (ignoring it)
        testPrivMsg = "@color=#0D4200;display-name=TWITCH_UserNaME;emotes=25:0-4,12-16/1902:6-10;subscriber=0;turbo=1;user-type=global_mod :foo!user@host PRIVMSG #channel :butt farts asdfadsf";
        msg = ChatParser.parse(testPrivMsg);

        assertEquals(msg.getType(), MessageType.Privmsg);
        assertEquals(msg.getPrivmsgMessage(), "butt farts asdfadsf");
        assertEquals(msg.getPrivmsgChannel(), "#channel");
    }
}