package com.dmh.tpf;

import java.util.Arrays;

/* this chat parser is very simplistic by design.
it only needs to care about two things
1) irc PING requests. need these to send a PONG back and not get disconnected
2) PRIVMSG msgs. this is where we will read our commands from. ideally the client will only be on one channel.
    even so the following are read:
    - channel
    - nick msg came from (in this case twitch username)

See the IrcMessage class
 */
public class ChatParser {
    private static MessageType getMessageType(String msgChunk) {
        if (msgChunk == null || msgChunk.isEmpty())
            return MessageType.Other;
        String santized = msgChunk.toLowerCase().trim(); // !!todo: invariant culture
        switch (santized) {
            case "ping":    return MessageType.Ping;
            case "privmsg": return MessageType.Privmsg;
            default:        return santized.chars().allMatch(c -> Character.isDigit(c)) ? MessageType.Numeric : MessageType.Other;
        }
    }

    private static String removeLeadingColon(String str) {
        if (str != null && str.length() > 1 && str.charAt(0) == ':')
            return str.substring(1);
        return str;
    }

    public static IrcMessage parse(String raw) {
        //https://tools.ietf.org/html/rfc2812

        //message    =  [ ":" prefix SPACE ] command [ params ] crlf
        if (raw == null || raw.isEmpty())
            return null;

        // oh god, thats all great but we need to handle twitch extensions (IRCv3 stuff)
        //@color=#0D4200;display-name=TWITCH_UserNaME;emotes=25:0-4,12-16/1902:6-10;subscriber=0;turbo=1;user-type=global_mod :twitch_username!twitch_username@twitch_username.tmi.twitch.tv PRIVMSG #channel :Kappa Keepo Kappa
        //@color=#0D4200;display-name=TWITCH_UserNaME;emote-sets=0,33,50,237,793,2126,3517,4578,5569,9400,10337,12239;subscriber=1;turbo=1;user-type=staff :tmi.twitch.tv USERSTATE #channel

        // handle IRCv3 stuff before split to avoid excessive copying and string interning
        // nasty. skip it and pretend it didnt exist
        if (raw.charAt(0) == '@')
            raw = raw.substring(raw.indexOf(' ') + 1);

        boolean hasPrefix = raw.charAt(0) == ':';

        String[] msgChunks = raw.split(" ");

        // we need at least prefix + command, or command + arg (eg, PING :arg, NOTICE *, etc)
        if (msgChunks.length < 2)
            return null;

        // turbo fuck hack: if its IRCv3 tagged, drop that chunk
        //if (msgChunks[0].length() > 1 && msgChunks[0].charAt(0) == '@') {
        //    String extendedCrap = msgChunks[0];
        //    String[] tmp = new String[msgChunks.length - 1];
        //    System.arraycopy(msgChunks, 1, tmp, 0, tmp.length);
        //    msgChunks = tmp;

            // dont forget to adjust raw too, so below doesnt mess up
        //    raw = raw.substring(extendedCrap.length() + 1); // add one to skip the space
        //}

        String prefix;
        String command;

        if (hasPrefix) {
            prefix = removeLeadingColon(msgChunks[0]);
            command = msgChunks[1];
        }
        else {
            prefix = "";
            command = msgChunks[0];
        }
        MessageType msgType = getMessageType(command);

        String params;
        int paramStart = prefix.length() + command.length() + (hasPrefix ? 2 : 1);

        if (paramStart >= raw.length())
            params = "";
        else
            params = raw.substring(paramStart).replaceAll("\r\n", "").trim();

        if (msgType == MessageType.Privmsg) {
            // if we got a privmsg, keep parsing into the parameters since we will be using
            // these messages the most
            // :foo!user@host PRIVMSG #channel :butt farts asdfadsf
            //                        ^start of params
            String[] paramsChunks = params.split(" ");

            // params chunks should always be a length of two or more
            // in production code i'd assert this 8)
            if (paramsChunks.length >= 2) {
                String chan = paramsChunks[0];

                // dont forget to remove : from the details we care about. just modify the arry here
                // instead of making the reduce any more complex
                paramsChunks[1] = removeLeadingColon(paramsChunks[1]);
                // since we're going to turn the array into a stream,
                // we can skip string join and use stream reduce instead
                String msg = Arrays.stream(paramsChunks)
                            .skip(1) // skip the channel
                            .reduce((s, acc) -> s + " " + acc).get(); // smash the chunks into one string
                return new IrcMessage(prefix, chan, msg);
            }
        }

        return new IrcMessage(msgType, prefix, command, params);
    }
}
