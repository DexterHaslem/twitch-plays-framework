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
    }
}