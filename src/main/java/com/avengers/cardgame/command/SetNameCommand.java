package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

/**
 * Команда /setname.
 */
public class SetNameCommand implements Command {

    /** Текст ошибки при невалидном имени. */
    private static final String ERROR = "Вы ввели некорректное имя. "
            + "Допустимо использовать до 128 символов латиницей.";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы сменили отображаемое имя на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /** Сервис валидации. */
    private final ValidationService validationService;

    public SetNameCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public Reply handle(CommandRequest request) {
        String newName = request.argument();

        if (newName.isEmpty() || !validationService.isValidName(newName)) {
            return new Reply(ERROR);
        }

        userService.updateDisplayName(request.userId(), newName);
        return new Reply(SUCCESS_PREFIX + newName);
    }
}