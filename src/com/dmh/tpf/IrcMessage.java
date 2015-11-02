package com.dmh.tpf;

// this class is immutable
public class IrcMessage {
    private MessageType type;
    private String source;
    private String command;
    private String parameters; // parameters are simply a flat string for our needs

    //hack
    private String privmsgChannel;
    private String privmsgMessage;

    public IrcMessage(MessageType type, String source, String command, String parameters) {
        this.type = type;
        this.source = source;
        this.command = command;
        this.parameters = parameters;
    }

    // specialized privmsg ctor, which we primarily work with
    public IrcMessage(String source, String channel, String msg) {
        this.type = MessageType.Privmsg;
        this.source = source;
        this.privmsgChannel = channel;
        this.privmsgMessage = msg;

        // still fill these in
        command = "PRIVMSG";
        parameters = channel + " " + msg;
    }

    public IrcMessage(MessageType type) {
        this.type = type;
    }

    public MessageType getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public String getShortNick() {
        if (source == null || source.isEmpty())
            return null;
        String[] chunks = source.split("!");
        return chunks.length > 0 ? chunks[0] : null;
    }
    public String getCommand() {
        return command;
    }

    public String getParameters() {
        return parameters;
    }

    public String getPrivmsgChannel() {
        return privmsgChannel;
    }

    public String getPrivmsgMessage() {
        return privmsgMessage;
    }
}
