package com.dmh.tpf;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatClient {
    // these should not change. lol 'should' in technology context
    private final String    server  = "irc.twitch.tv";
    private final int       port    = 6667;

    private String nick;
    private String oauthToken;
    private String channel;
    private boolean isRunning;
    private Thread readThread;

    // net state
    // why bother having any writer at all if we're just listening to chat?
    // we still need to respond to irc PINGs with PONG to not be disconnected
    private Socket              socket;
    private InputStreamReader   socketReader;
    private OutputStreamWriter  socketWriter;
    private BufferedReader      bufferedReader;
    private BufferedWriter      bufferedWriter;
    private final Object        syncContext = new Object();

    private List<ChatLineListener> listeners = new ArrayList<>();

    public boolean connect() {
        try {
            socket = new Socket(server, port);
            socketReader = new InputStreamReader(socket.getInputStream());
            bufferedReader = new BufferedReader(socketReader);

            socketWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(socketWriter);
            return socket.isConnected();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean disconnect() {
        isRunning = false;
        if (socket == null || !socket.isConnected())
            return true;
        try {
            socket.close();
            socketReader.close();
            bufferedReader.close();
            socketWriter.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return socket.isClosed();
    }

    public ChatClient(String nick, String oauth, String chan) {
        this.nick           = nick;
        this.oauthToken     = oauth;
        this.channel        = chan;
    }

    // dont append \r\n this will handle it
    public void sendLine(String line) {
        if (socket == null || !socket.isConnected())
            return;

        try {
            socketWriter.write(line + "\r\n");
            socketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // for this I just went with an observer pattern instead of a full blown java event.
    // the func will be executed in reader thread
    public void addListener(ChatLineListener listener) {
        synchronized (syncContext) {
            if (!listeners.contains(listener))
                listeners.add(listener);
        }
    }

    public void removeListener(ChatLineListener listener) {
        synchronized (syncContext) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(String newLine) {
        synchronized (syncContext) {
            for (ChatLineListener cl : listeners)
                cl.onChatLine(newLine);
        }
    }

    public void readLoopThread() {
        synchronized (syncContext) {
            if (readThread != null && readThread.isAlive())
                isRunning = false;

            readThread = new Thread(() -> {
                // dont forget to introduce a race condition so the project isnt too easy
                synchronized (syncContext) {
                    isRunning = true;
                }

                while (isRunning) {
                    try {
                        String freshLine = bufferedReader.readLine();
                        if (!freshLine.isEmpty())
                            notifyListeners(freshLine);
                    } catch (IOException e) {
                        e.printStackTrace();
                        synchronized (syncContext) {
                            isRunning = false;
                            return;
                        }
                    }
                }

                synchronized (syncContext) {
                    isRunning = false;
                }
            });

            readThread.run();
        }
    }

    public void stop() {
        synchronized (syncContext) {
            readThread = null;
            isRunning = false;
        }
    }
}
