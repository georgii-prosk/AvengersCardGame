package com.avengers.cardgame.service;

import com.avengers.cardgame.bot.Bot;

/**
 * Логика обработки нажатий на inline-кнопки.
 * <p>
 * Не зависит от платформы: принимает обезличенные данные
 * и отвечает через переданного бота.
 */
public interface CallbackProcessingService {

    /**
     * Обрабатывает нажатие на кнопку.
     *
     * @param userId       идентификатор пользователя
     * @param chatId       идентификатор чата
     * @param callbackData данные кнопки
     * @param bot          бот для отправки ответов
     */
    void processCallback(long userId, long chatId, String callbackData, Bot bot);
}
