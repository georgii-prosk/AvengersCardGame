package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class StartCommand implements Command {

    @Override
    public String name() {
        return "/start";
    }

    @Override
    public void execute(TelegramClient client, long chatId, long userId, String text)
            throws TelegramApiException {

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