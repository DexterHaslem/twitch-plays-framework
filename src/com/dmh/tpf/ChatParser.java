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

        String[] msgChunks = raw.split(" ");

        // we need at least a prefix and command
        if (msgChunks.length < 2)
            return null;

        String prefix = removeLeadingColon(msgChunks[0]);

        String command = msgChunks[1];
        MessageType msgType = getMessageType(command);

        String params;
        int paramStart = prefix.length() + command.length() + 2;
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