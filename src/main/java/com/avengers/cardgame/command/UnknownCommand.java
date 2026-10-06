package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class UnknownCommand implements Command {

    @Override
    public String name() {
        return "/unknown";
    }

    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException {

        String message = "Неизвестная команда. Введите /help для списка команд.";

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        client.execute(sendMessage);
    }
}