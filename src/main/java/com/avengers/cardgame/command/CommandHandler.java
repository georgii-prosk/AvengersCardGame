package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.AppException;

/**
 * Обработчик команды бота.
 */
public interface CommandHandler {

    /**
     * Обрабатывает команду, отправляя ответы через {@code context.bot()}.
     *
     * @param context контекст выполнения команды
     * @throws AppException при доменной ошибке
     */
    void handle(CommandContext context) throws AppException;
}