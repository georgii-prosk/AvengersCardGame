package com.avengers.cardgame.exception;

/** Техническая ошибка при отправке сообщения через Telegram. */
public class BotException extends RuntimeException {
    public BotException(String message, Throwable cause) {
        super(message, cause);
    }
}
