package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.AppException;

/**
 * Обработчик конкретной команды.
 */
public interface CommandHandler {

    /**
     * Обрабатывает команду, отправляя ответы через {@code context.bot()}.
     *
     * @param context контекст выполнения команды (содержит ID пользователя, чата и бота)
     * @throws AppException при возникновении доменной ошибки
     */
    void handle(CommandContext context) throws AppException;
}