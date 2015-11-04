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