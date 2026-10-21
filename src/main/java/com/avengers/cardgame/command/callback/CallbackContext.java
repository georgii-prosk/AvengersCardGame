package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.bot.Bot;

/**
 * Контекст обработки нажатия на inline-кнопку.
 *
 * @param userId       идентификатор пользователя
 * @param chatId       идентификатор чата
 * @param callbackData полные данные кнопки вида {@code prefix:argument}
 * @param bot          бот для отправки ответов
 */
public record CallbackContext(long userId, long chatId, String callbackData, Bot bot) {

    /** Разделитель префикса и аргумента в данных кнопки. */
    private static final String SEPARATOR = ":";

    /**
     * Возвращает часть данных кнопки после первого разделителя.
     *
     * @return аргумент или пустая строка, если разделителя нет
     */
    public String argument() {
        int index = callbackData.indexOf(SEPARATOR);
        return index == -1 ? "" : callbackData.substring(index + SEPARATOR.length());
    }
}
