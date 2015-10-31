package com.dmh.tpf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Dexter on 10/31/2015.
 */
public class Config {
    public static final String GAME_EXE_CFG = "game.txt";
    public static final String COMMAND_CFG = "commands.txt";

    public String getGameExecutableName() {
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

    public List<GameCommand> getCommands() {
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
