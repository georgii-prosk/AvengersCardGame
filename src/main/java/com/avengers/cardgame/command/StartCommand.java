package com.avengers.cardgame.command;

import com.avengers.cardgame.service.UserService;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand implements Command {

    private final UserService userService;

    public StartCommand(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String name() {
        return "/start";
    }

    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        String defaultName = (userName == null || userName.isBlank()) ? "Avenger" : userName;
        userService.registerIfAbsent(userId, defaultName);

        String message = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        client.execute(sendMessage);
    }
}