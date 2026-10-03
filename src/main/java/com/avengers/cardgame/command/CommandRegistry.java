package com.avengers.cardgame.command;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry() {
        register(new StartCommand());
    }

    private void register(Command command) {
        commands.put(command.name(), command);
    }

    public Command resolve(String text) {
        if (text == null || text.isBlank()) {
            return new UnknownCommand();
        }
        String commandName = text.trim().split("\\s+")[0].toLowerCase();
        return commands.getOrDefault(commandName, new UnknownCommand());
    }
}