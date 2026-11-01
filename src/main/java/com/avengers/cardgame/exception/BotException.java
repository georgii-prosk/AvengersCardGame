package com.avengers.cardgame.exception;

/** Техническая ошибка при отправке сообщения через бота. */
public class BotException extends RuntimeException {
    /**
     * @param message описание ошибки
     * @param cause исходное исключение
     */
    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
