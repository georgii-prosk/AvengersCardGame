package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.service.UserService;

/**
 * Команда /setbio.
 */
public class SetBioCommand implements CommandHandler {

    /** Текст ошибки при невалидном bio. */
    private static final String ERROR = "Вы ввели некорректное описание профиля. "
            + "Допустимо использовать до 256 символов латиницей или кириллицей "
            + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
            + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы обновили описание своего профиля на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /**
     * @param userService сервис пользователей
     */
    public SetBioCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Reply handle(long userId, String userName, String argument)
            throws UserNotFoundException {
        Bio newBio;
        try {
            newBio = new Bio(argument);
        } catch (IllegalArgumentException e) {
            return new Reply(ERROR);
        }

        userService.updateBio(userId, newBio);
        return new Reply(SUCCESS_PREFIX + newBio.value());
    }
}