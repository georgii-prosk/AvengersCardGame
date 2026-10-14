package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: пользователь с таким идентификатором уже существует.
 */
public class UserAlreadyExistsException extends Exception {

    /** Идентификатор существующего пользователя. */
    private final long userId;

    /**
     * @param userId идентификатор существующего пользователя
     */
    public UserAlreadyExistsException(long userId) {
        super("User already exists: userId=" + userId);
        this.userId = userId;
    }

    /**
     * @return идентификатор существующего пользователя
     */
    public long userId() {
        return userId;
    }
}