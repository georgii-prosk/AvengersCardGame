package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();

    public CommandRegistry(UserService userService, ValidationService validationService) {
        register(new StartCommand(userService));
        register(new HelpCommand());
        register(new ProfileCommand(userService));
        register(new SetNameCommand(userService, validationService));
        register(new SetBioCommand(userService, validationService));
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