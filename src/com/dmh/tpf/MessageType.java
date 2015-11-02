package com.dmh.tpf;

// represents data parsed out of an irc line. we only care about a few msgs. see chat parser for details
public enum MessageType {
    Ping,
    Privmsg,
    Numeric,
    Other,
}
