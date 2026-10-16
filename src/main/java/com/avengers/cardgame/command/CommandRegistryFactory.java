package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;

/**
 * Создаёт {@link CommandRegistry} со всеми командами бота.
 */
public class CommandRegistryFactory {

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public CommandRegistryFactory(UserService userService) {
        this.userService = userService;
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
        registry.register("/setname", wrap(new SetNameCommand(userService)));
        registry.register("/setbio", wrap(new SetBioCommand(userService)));

        return registry;
    }

    /**
     * Оборачивает команду в {@link RegisteringCommand}.
     *
     * @param command команда
     * @return обёрнутый обработчик
     */
    private CommandHandler wrap(CommandHandler command) {
        return new RegisteringCommand(command, userService);
    }
}