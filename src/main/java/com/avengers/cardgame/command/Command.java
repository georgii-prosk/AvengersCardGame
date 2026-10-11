package com.avengers.cardgame.command;

/**
 * Команда бота.
 */
public interface Command {

    /**
     * Обрабатывает запрос.
     *
     * @param request данные запроса
     * @return ответ пользователю
     */
    Reply handle(CommandRequest request);
}