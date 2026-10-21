package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: некорректное описание профиля.
 */
public class InvalidBioException extends AppException {

    /**
     * @param message описание ошибки
     */
    public InvalidBioException(String message) {
        super(message);
    }
}