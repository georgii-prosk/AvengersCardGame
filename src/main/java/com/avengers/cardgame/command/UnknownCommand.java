package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Заглушка для неизвестных команд.
 * <p>
 * Возвращается {@link CommandRegistry}, если входящий текст не совпал ни с одной
 * зарегистрированной командой. Сообщает пользователю о необходимости ввести
 * {@code /help}.
 */
public class UnknownCommand implements Command {

    /**
     * {@inheritDoc}
     *
     * @return {@code "/unknown"}
     */
    @Override
    public String name() {
        return "/unknown";
    }

    /**
     * Отправляет пользователю сообщение о неизвестной команде.
     *
     * @param client   клиент Telegram
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param text     текст сообщения
     * @throws TelegramApiException при ошибке отправки сообщения
     */
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