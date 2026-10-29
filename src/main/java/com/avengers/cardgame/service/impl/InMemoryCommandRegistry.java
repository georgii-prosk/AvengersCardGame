package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.ResolvedCommand;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** Реализация реестра команд на основе HashMap. */
public class InMemoryCommandRegistry implements CommandRegistry {

    private final Map<String, CommandHandler> commands = new HashMap<>();

    @Override
    public void register(String name, CommandHandler command) {
        commands.put(name, command);
    }

    @Override
    public Optional<ResolvedCommand> resolve(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String trimmed = text.trim();
        int spaceIndex = trimmed.indexOf(' ');
        String name = (spaceIndex == -1 ? trimmed : trimmed.substring(0, spaceIndex)).toLowerCase(Locale.ROOT);
        CommandHandler command = commands.get(name);
        if (command == null) {
            return Optional.empty();
        }
        String argument = (spaceIndex == -1) ? "" : trimmed.substring(spaceIndex + 1).trim();
        return Optional.of(new ResolvedCommand(command, argument));
    }
}