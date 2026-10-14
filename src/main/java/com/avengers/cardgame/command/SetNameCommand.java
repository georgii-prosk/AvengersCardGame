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