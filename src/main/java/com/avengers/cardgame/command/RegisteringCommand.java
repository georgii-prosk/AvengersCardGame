package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.util.Constants;

/**
 * Декоратор команды: регистрирует пользователя перед обработкой.
 */
public class RegisteringCommand implements CommandHandler {

    /** Оборачиваемый обработчик. */
    private final CommandHandler delegate;

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param delegate    оборачиваемый обработчик
     * @param userService сервис пользователей
     */
    public RegisteringCommand(CommandHandler delegate, UserService userService) {
        this.delegate = delegate;
        this.userService = userService;
    }

    @Override
    public Reply handle(long userId, String userName, String argument)
            throws UserNotFoundException, UserAlreadyExistsException {
        PlayerName name = resolveName(userName);
        userService.registerIfAbsent(userId, name);
        return delegate.handle(userId, userName, argument);
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
                return new PlayerName(userName);
            } catch (IllegalArgumentException ignored) {
                // имя от пользователя невалидно — используем значение по умолчанию
            }
        }
        return new PlayerName(Constants.DEFAULT_USER_NAME);
    }
}