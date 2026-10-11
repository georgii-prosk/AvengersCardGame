package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.util.Constants;

/**
 * Декоратор команды: регистрирует пользователя перед обработкой.
 */
public class RegisteringCommand implements Command {

    /** Оборачиваемая команда. */
    private final Command delegate;

    /** Сервис пользователей. */
    private final UserService userService;

    public RegisteringCommand(Command delegate, UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public Reply handle(CommandRequest request) {
        String name = (request.userName() == null || request.userName().isBlank())
                ? Constants.DEFAULT_USER_NAME
                : request.userName();
        userService.registerIfAbsent(request.userId(), name);
        return delegate.handle(request);
    }
}