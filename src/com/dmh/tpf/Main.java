package com.dmh.tpf;

import java.util.*;
import java.util.stream.Stream;

public class Main {
    private static HashMap<String, GameCommand> queue = new HashMap<>();

    private static final int QUEUE_RATE_MS = 1000;

    public static void main(String[] args) {

        /* rough outline of order of steps

        1 - check configs
        2 - make sure game is running
        3 - start irc
        4 - turn relevant irc chat into commands as long as connected

        im going to write a monster then refactor it once working
         */
        final String errorMsg = "Failed to load %s, please create the configuration. See the repo wiki for details\n";
        String gameName = Config.getGameExecutableName();
        if (gameName == null) {
            System.err.printf(errorMsg, Config.GAME_EXE_CFG);
            System.exit(1);
        }

        String[] chatConfig = Config.getChatConfig();
        if (chatConfig == null || chatConfig.length < 3) {
            System.err.printf(errorMsg, Config.CHAT_CFG);
            System.exit(2);
        }

        List<GameCommand> gameCommands = Config.getCommands();
        if (gameCommands == null) {
            System.err.printf(errorMsg, Config.COMMAND_CFG);
            System.exit(3);
        }

        InputDirector inputDirector = new Win32InputDirector(gameName);

        if (!inputDirector.findInstance()) {
            System.err.printf("failed to find running instance of %s. run game before starting\n", Config.GAME_EXE_CFG);
            System.exit(4);
        }

        // startup irc
        ChatClient chatClient = new ChatClient(chatConfig[0], chatConfig[1], chatConfig[2]);
        if (!chatClient.connect()) {
            System.err.println("failed to connect to twitch IRC, check chat config");
            System.exit(5);
        }

        chatClient.addListener(rawLine -> tryQueueMsg(ChatParser.parse(rawLine)));
        chatClient.readLoopThread();
        chatClient.sendAuth();
        chatClient.joinChannel(chatClient.getChannel()); // its like this for unit tests i promise

        while (chatClient.isRunning()) {
            GameCommand[] gameCommandsFromChat;
            synchronized (queue) {
                gameCommandsFromChat = (GameCommand[])queue.values().toArray();
                queue.clear();
            }

            if (gameCommandsFromChat.length > 1) {
                int mostPopularKey = getMostPopularKey(gameCommandsFromChat);
                inputDirector.sendKey(mostPopularKey);
            }
            else if (gameCommandsFromChat.length == 1) {
                inputDirector.sendKey(gameCommandsFromChat[0].getKeycode());
            }

            try {
                Thread.sleep(QUEUE_RATE_MS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }

        System.out.println("tpf: done");
    }

    private static int getMostPopularKey(GameCommand[] fromChat) {
        // this is kinda dumb. probably easier if i implemented equality
        // hash key is.. keycode, so we dont have to go dig it up later
        HashMap<Integer, Integer> tally = new HashMap<>();
        for (GameCommand gc : fromChat) {
            int keyCode = gc.getKeycode();
            int curVal = tally.getOrDefault(keyCode, 0);
            tally.put(keyCode, curVal + 1);
        }

        // lord in heaven
        Stream<Map.Entry<Integer, Integer>> sorted = tally.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

        return (int)sorted.toArray()[0];
    }

    private static void tryQueueMsg(IrcMessage ircMsg) {
    }
}
