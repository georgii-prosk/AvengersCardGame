package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: пользователь не найден.
 * <p>
 * Возникает, когда операция требует существующего пользователя,
 * но его нет в хранилище.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Идентификатор ненайденного пользователя.
     */
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