package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class SetBioCommand implements Command {

    private final UserService userService;
    private final ValidationService validationService;

    public SetBioCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public String name() {
        return "/setbio";
    }

    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        String newBio = extractArgument(text, "/setbio");

        if (newBio.isEmpty() || !validationService.isValidBio(newBio)) {
            send(client, chatId,
                    "Вы ввели некорректное описание профиля. Допустимо использовать до 256 символов " +
                            "латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, " +
                            "спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'\"-_=+).");
            return;
        }

        userService.registerIfAbsent(userId, "Avenger");
        userService.updateBio(userId, newBio);

        send(client, chatId, "Вы обновили описание своего профиля на " + newBio);
    }

    private String extractArgument(String text, String command) {
        String trimmed = text.trim();
        if (trimmed.length() <= command.length()) {
            return "";
        }
        return trimmed.substring(command.length()).trim();
    }

    private void send(TelegramClient client, long chatId, String message)
            throws TelegramApiException {
        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
        client.execute(sendMessage);
    }
}