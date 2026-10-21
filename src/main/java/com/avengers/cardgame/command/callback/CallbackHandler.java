package com.avengers.cardgame.command.callback;

import com.avengers.cardgame.exception.AppException;

/**
 * Обработчик нажатия на inline-кнопку.
 */
public interface CallbackHandler {

    /**
     * Обрабатывает нажатие, отправляя ответы через {@code context.bot()}.
     *
     * @param context контекст нажатия
     * @throws AppException при доменной ошибке
     */
    void handle(CallbackContext context) throws AppException;
}
