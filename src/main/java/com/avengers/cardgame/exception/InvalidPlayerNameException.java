package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: некорректное имя игрока.
 */
public class InvalidPlayerNameException extends AppException {

    /**
     * @param message описание ошибки
     */
    public InvalidPlayerNameException(String message) {
        super(message);
    }
}