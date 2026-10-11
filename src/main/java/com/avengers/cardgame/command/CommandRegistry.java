package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Реестр команд бота.
 */
public class CommandRegistry {

    private final Map<String, Command> commands = new HashMap<>();
    private final UserService userService;

    public CommandRegistry(UserService userService, ValidationService validationService) {
        this.userService = userService;

        register("/start", wrap(new StartCommand()));
        register("/help", new HelpCommand());
        register("/profile", wrap(new ProfileCommand(userService)));
        register("/setname", wrap(new SetNameCommand(userService, validationService)));
        register("/setbio", wrap(new SetBioCommand(userService, validationService)));
    }

    /**
     * Регистрирует команду под указанным именем.
     *
     * @param name    имя команды вида {@code "/start"}
     * @param command реализация команды
     */
    public void register(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Определяет команду по тексту сообщения.
     *
     * @param text полный текст сообщения
     * @return команда, если найдена
     */
    public Optional<Command> resolve(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        String commandName = text.trim().split("\\s+")[0].toLowerCase();
        return Optional.ofNullable(commands.get(commandName));
    }

    /**
     * Оборачивает команду в {@link RegisteringCommand}.
     */
    private Command wrap(Command command) {
        return new RegisteringCommand(command, userService);
    }
}