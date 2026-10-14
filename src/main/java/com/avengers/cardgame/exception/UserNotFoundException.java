package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: пользователь не найден.
 */
public class UserNotFoundException extends Exception {

    /** Идентификатор ненайденного пользователя. */
    private final long userId;

    /**
     * @param userId идентификатор ненайденного пользователя
     */
    public UserNotFoundException(long userId) {
        super("User not found: userId=" + userId);
        this.userId = userId;
    }

    /**
     * @return идентификатор ненайденного пользователя
     */
    public long userId() {
        return userId;
    }
}