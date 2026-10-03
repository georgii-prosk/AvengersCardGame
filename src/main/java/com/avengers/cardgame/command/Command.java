package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public interface Command {

    String name();

    void execute(TelegramClient client, long chatId, long userId, String text)
            throws TelegramApiException;
}