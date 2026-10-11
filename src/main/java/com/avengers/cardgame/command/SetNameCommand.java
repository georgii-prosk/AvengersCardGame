package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;

/**
 * Команда /setname.
 */
public class SetNameCommand implements Command {

    private static final String COMMAND_NAME = "/setname";

    private final UserService userService;
    private final ValidationService validationService;

    public SetNameCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public Reply handle(CommandRequest request) {
        String newName = extractArgument(request.text(), COMMAND_NAME);

        if (newName.isEmpty() || !validationService.isValidName(newName)) {
            return new Reply("Вы ввели некорректное имя. "
                    + "Допустимо использовать до 128 символов латиницей.");
        }

        userService.updateDisplayName(request.userId(), newName);
        return new Reply("Вы сменили отображаемое имя на " + newName);
    }

    private String extractArgument(String text, String command) {
        String trimmed = text.trim();
        if (trimmed.length() <= command.length()) {
            return "";
        }
        return trimmed.substring(command.length()).trim();
    }
}