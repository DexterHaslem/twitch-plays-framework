package com.dmh.tpf;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

public class Main {
    private HashMap<String, GameCommand> queue = new HashMap<>();

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


    }
}
