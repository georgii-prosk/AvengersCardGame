package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: не удалось отправить сообщение через бота.
 */
public class BotException extends AppException {

    /**
     * @param message описание ошибки
     * @param cause   причина
     */
    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}