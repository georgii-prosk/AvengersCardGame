package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /setname.
 */
public class SetNameCommand implements CommandHandler {

    /** Текст ошибки при невалидном имени. */
    private static final String ERROR = "Вы ввели некорректное имя. "
            + "Допустимо использовать до 128 символов латиницей.";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы сменили отображаемое имя на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public SetNameCommand(UserService userService) {
        this.userService = userService;
    }

    /**
     * Валидирует новое имя и обновляет его в профиле пользователя.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param argument новое имя
     * @return ответ об успехе или ошибке валидации
     * @throws UserNotFoundException если пользователь не найден
     */
    @Override
    public Reply handle(long userId, String userName, String argument)
            throws UserNotFoundException {
        PlayerName newName;
        try {
            newName = new PlayerName(argument);
        } catch (IllegalArgumentException e) {
            return new Reply(ERROR);
        }

        userService.updateDisplayName(userId, newName);
        return new Reply(SUCCESS_PREFIX + newName.value());
    }
}