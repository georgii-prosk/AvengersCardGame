package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class SetNameCommand implements Command {

    private final UserService userService;
    private final ValidationService validationService;

    public SetNameCommand(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @Override
    public String name() {
        return "/setname";
    }

    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        // Отрезаем "/setname" и берём всё, что после первого пробела
        String newName = extractArgument(text, "/setname");

        if (newName.isEmpty() || !validationService.isValidName(newName)) {
            send(client, chatId,
                    "Вы ввели некорректное имя. Допустимо использовать до 128 символов латиницей.");
            return;
        }

        userService.registerIfAbsent(userId, "Avenger");
        userService.updateDisplayName(userId, newName);

        send(client, chatId, "Вы сменили отображаемое имя на " + newName);
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