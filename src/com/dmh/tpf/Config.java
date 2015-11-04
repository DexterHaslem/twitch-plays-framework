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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    public static final String GAME_EXE_CFG = "config/game.txt";
    public static final String COMMAND_CFG  = "config/commands.txt";
    public static final String CHAT_CFG  = "config/chat.txt";

    public static String getGameExecutableName() {
        Path gamePath = Paths.get(GAME_EXE_CFG);
        if (!Files.exists(gamePath))
            return null;
        try {
            List<String> lines = Files.readAllLines(gamePath);

            if (lines.isEmpty())
                return null;

            return lines.get(0).trim();
        } catch (IOException ex) {
            return null;
        }
    }

    //note: cba to create a class, returns nick,oauth,chan
    public static String[] getChatConfig() {
        Path chatPath = Paths.get(CHAT_CFG);
        if (!Files.exists(chatPath))
            return null;
        try {
            List<String> lines = Files.readAllLines(chatPath);

            if (lines.isEmpty())
                return null;

            String firstLine = lines.get(0).trim();
            // format is "nick oauth chan" thats it
            String[] chunks = firstLine.split(" ");
            return chunks.length != 3 ? null : chunks;
        } catch (IOException ex) {
            return null;
        }
    }

    public static List<GameCommand> getCommands() {
        Path cmdPath = Paths.get(COMMAND_CFG);
        if (!Files.exists(cmdPath))
            return null;

        List<GameCommand> ret = new ArrayList<>();
        try {
            // fromString does not return null
            ret.addAll(Files.readAllLines(cmdPath).stream().map(GameCommand::fromString).collect(Collectors.toList()));
        } catch (IOException ex) {
            return null;
        }

        return ret;
    }
}
