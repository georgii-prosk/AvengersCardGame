package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InvalidPlayerNameException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;
import com.avengers.cardgame.service.UserService;

/**
 * Декоратор команды: регистрирует пользователя перед обработкой.
 */
public class RegisteringCommandHandler implements CommandHandler {

    /** Имя пользователя по умолчанию. */
    private static final String DEFAULT_USER_NAME = "Avenger";

    /** Оборачиваемый обработчик. */
    private final CommandHandler delegate;

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param delegate    оборачиваемый обработчик
     * @param userService сервис пользователей
     */
    public RegisteringCommandHandler(CommandHandler delegate, UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public void handle(CommandContext context) throws AppException {
        PlayerName name = resolveName(context.userName());
        userService.registerIfAbsent(context.userId(), name);
        delegate.handle(context);
    }

    /**
     * Возвращает {@link PlayerName} для регистрации: имя пользователя,
     * если оно валидно, иначе — имя по умолчанию.
     *
     * @param userName имя пользователя, может быть {@code null}
     * @return валидное отображаемое имя
     */
    private PlayerName resolveName(String userName) {
        if (userName != null && !userName.isBlank()) {
            try {
                return PlayerName.of(userName);
            } catch (InvalidPlayerNameException ignored) {
                // имя от пользователя невалидно — используем значение по умолчанию
            }
        }
        try {
            return PlayerName.of(DEFAULT_USER_NAME);
        } catch (InvalidPlayerNameException exception) {
            throw new IllegalStateException("Default name is invalid", exception);
        }
    }
}