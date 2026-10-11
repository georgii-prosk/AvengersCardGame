package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

/**
 * Команда /setbio.
 */
public class SetBioCommand implements Command {

    private static final String COMMAND_NAME = "/setbio";

    private final UserService userService;
    private final ValidationService validationService;

    public SetBioCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public Reply handle(CommandRequest request) {
        String newBio = extractArgument(request.text(), COMMAND_NAME);

        if (newBio.isEmpty() || !validationService.isValidBio(newBio)) {
            return new Reply("Вы ввели некорректное описание профиля. "
                    + "Допустимо использовать до 256 символов латиницей или кириллицей "
                    + "+ эмодзи и знаков препинания, пробелов, цифр, спецсимволы "
                    + "(/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).");
        }

        userService.updateBio(request.userId(), newBio);
        return new Reply("Вы обновили описание своего профиля на " + newBio);
    }

    private String extractArgument(String text, String command) {
        String trimmed = text.trim();
        if (trimmed.length() <= command.length()) {
            return "";
        }
        return trimmed.substring(command.length()).trim();
    }
}