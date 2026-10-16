package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.util.Constants;

/**
 * Декоратор команды: регистрирует пользователя перед обработкой.
 */
public class RegisteringCommand extends SafeCommandHandler {

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

    /**
     * Регистрирует пользователя и вызывает делегата.
     *
     * @param context контекст выполнения команды
     * @throws AppException при доменной ошибке
     */
    @Override
    protected void doHandle(CommandContext context) throws AppException {
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
                return new PlayerName(userName);
            } catch (IllegalArgumentException ignored) {
                // имя от пользователя невалидно — используем значение по умолчанию
            }
        }
        return new PlayerName(Constants.DEFAULT_USER_NAME);
    }
}