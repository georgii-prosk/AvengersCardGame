package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

/**
 * Создаёт {@link CommandRegistry} со всеми командами бота.
 */
public class CommandRegistryFactory {

    /** Сервис пользователей. */
    private final UserService userService;

    /** Сервис валидации. */
    private final ValidationService validationService;

    public CommandRegistryFactory(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    /**
     * Создаёт и наполняет реестр команд.
     *
     * @return готовый реестр
     */
    public CommandRegistry create() {
        CommandRegistry registry = new CommandRegistry();

        registry.register("/start", wrap(new StartCommand()));
        registry.register("/help", new HelpCommand());
        registry.register("/profile", wrap(new ProfileCommand(userService)));
        registry.register("/setname",
                wrap(new SetNameCommand(userService, validationService)));
        registry.register("/setbio",
                wrap(new SetBioCommand(userService, validationService)));

        return registry;
    }

    /**
     * Оборачивает команду в {@link RegisteringCommand}.
     *
     * @param command команда
     * @return обёрнутая команда
     */
    private Command wrap(Command command) {
        return new RegisteringCommand(command, userService);
    }
}