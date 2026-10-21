package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: некорректные данные пользователя.
 */
public class InvalidUserException extends AppException {

    /**
     * @param message описание ошибки
     */
    public InvalidUserException(String message) {
        super(message);
    }
}