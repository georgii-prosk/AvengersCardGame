package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

/**
 * Команда /setbio.
 */
public class SetBioCommand implements Command {

    /** Текст ошибки при невалидном bio. */
    private static final String ERROR = "Вы ввели некорректное описание профиля. "
            + "Допустимо использовать до 256 символов латиницей или кириллицей "
            + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
            + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).";

    /** Префикс успешного ответа. */
    private static final String SUCCESS_PREFIX = "Вы обновили описание своего профиля на ";

    /** Сервис пользователей. */
    private final UserService userService;

    /** Сервис валидации. */
    private final ValidationService validationService;

    public SetBioCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }


    /**
     * Валидирует новое bio и обновляет его в профиле.
     *
     * @param request запрос с новым bio в аргументе
     * @return ответ об успехе или ошибке валидации
     */
    @Override
    public Reply handle(CommandRequest request) {
        String newBio = request.argument();

        if (newBio.isEmpty() || !validationService.isValidBio(newBio)) {
            return new Reply(ERROR);
        }

        userService.updateBio(request.userId(), newBio);
        return new Reply(SUCCESS_PREFIX + newBio);
    }
}