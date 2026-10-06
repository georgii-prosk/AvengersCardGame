package com.avengers.cardgame.command;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Контракт для команд Telegram-бота.
 * <p>
 * Каждая реализация обрабатывает отдельное действие в ответ на текстовое
 * сообщение пользователя. Команды регистрируются в {@link CommandRegistry}.
 *
 * @see CommandRegistry
 */
public interface Command {

    /**
     * Возвращает имя команды, по которому она распознаётся.
     *
     * @return уникальное имя команды (например, {@code "/start"})
     */
    String name();

    /**
     * Выполняет логику команды.
     *
     * @param client   клиент Telegram для отправки сообщений
     * @param chatId   идентификатор чата
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя в Telegram (может быть {@code null})
     * @param text     полный текст сообщения с аргументами
     * @throws TelegramApiException при ошибке взаимодействия с Telegram API
     */
    void execute(TelegramClient client, long chatId, long userId, String userName, String text)
            throws TelegramApiException;
}