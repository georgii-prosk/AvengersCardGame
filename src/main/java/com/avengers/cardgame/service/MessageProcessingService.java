package com.avengers.cardgame.service;

import com.avengers.cardgame.bot.Bot;

/**
 * Логика обработки входящих сообщений.
 * <p>
 * Не зависит от платформы: принимает обезличенные данные
 * и отвечает через переданного бота.
 */
public interface MessageProcessingService {

    /**
     * Обрабатывает сообщение пользователя.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя, может быть {@code null}
     * @param chatId   идентификатор чата
     * @param text     полный текст сообщения
     * @param bot      бот для отправки ответов
     */
    void processMessage(long userId, String userName, long chatId, String text, Bot bot);
}