package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Команда {@code /help} — выводит список доступных команд бота с кратким описанием.
 */
public class HelpCommand implements Command {

    @Override
    public String name() {
        return "/help";
    }

    /**
     * Отправляет пользователю справочное сообщение со списком команд.
     *
     * @param client   клиент Telegram
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param text     текст сообщения
     * @throws TelegramApiException при ошибке отправки сообщения
     */
    @Override
    public void execute(TelegramClient client, long chatId, long userId, String userName,String text)
            throws TelegramApiException {

        String message = """
                /start - запуск бота.
                /help - помощь.
                /profile - информация о твоём профиле в боте.
                /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
                /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
                """;

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();

        client.execute(sendMessage);
    }
}