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
    // we still need to respond to irc PINGs with PONG to not be disconnected, and register
    private Socket              socket;
    private InputStreamReader   socketReader;
    private OutputStreamWriter  socketWriter;
    private BufferedReader      bufferedReader;
    private BufferedWriter      bufferedWriter;
    private final Object        syncContext = new Object();

    private List<ChatLineListener> listeners = new ArrayList<>();

    public boolean isRunning() {
        synchronized (syncContext) {
            return isRunning;
        }
    }

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

    public boolean sendAuth() {
        if (socket.isClosed())
            return false;
        sendLine("PASS " + oauthToken);
        sendLine("NICK " + nick);
        return true;
    }

    public boolean disconnect() {
        isRunning = false;
        if (socket == null || socket.isClosed())
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
        if (socket == null || socket.isClosed())
            return;

        try {
            socketWriter.write(line + "\r\n");
            socketWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void joinChannel(String channelName) {
        sendLine("JOIN " + channelName);
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
            // arg nasty race
            //if (readThread != null && readThread.isAlive())
            //    isRunning = false;

            readThread = new Thread(() -> {
                // dont forget to introduce a race condition so the project isnt too easy
                synchronized (syncContext) {
                    isRunning = true;
                }

                do {
                    try {

                        String freshLine = bufferedReader.readLine();
                        // on graceful disconnect readLine returns null
                        if (freshLine == null) {
                            synchronized (syncContext) {
                                isRunning = false;
                                return;
                            }
                        }

                        if (!freshLine.isEmpty())
                            notifyListeners(freshLine);
                    } catch (IOException e) {
                        e.printStackTrace();
                        synchronized (syncContext) {
                            isRunning = false;
                            return;
                        }
                    }
                } while (isRunning); // no lock here is intentional

                // fallthrough
                synchronized (syncContext) {
                    isRunning = false;
                }
            });

            // oops that was syncronous
            // readThread.run();
            readThread.start();
        }
    }

    public void stop() {
        synchronized (syncContext) {
            readThread = null;
            isRunning = false;
        }
    }

    public String getChannel() {
        return channel;
    }
}
