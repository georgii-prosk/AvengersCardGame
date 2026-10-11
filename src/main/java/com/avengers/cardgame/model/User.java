package com.avengers.cardgame.model;

import java.time.LocalDateTime;

/**
 * Профиль игрока.
 *
 * @param userId      идентификатор пользователя на платформе
 * @param displayName отображаемое имя
 * @param bio         описание профиля
 * @param balance     баланс игровой валюты («щиты»)
 * @param cardCount   количество карточек в коллекции
 * @param createdAt   дата и время создания записи
 * @param updatedAt   дата и время последнего обновления записи
 */
public record User(
        long userId,
        String displayName,
        String bio,
        int balance,
        int cardCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Создаёт нового игрока с минимальными данными.
     *
     * @param userId      идентификатор пользователя
     * @param displayName отображаемое имя
     */
    public User(long userId, String displayName) {
        this(userId, displayName, "", 0, 0, null, null);
    }

    /**
     * Возвращает копию пользователя с новым отображаемым именем.
     *
     * @param newName новое имя
     * @return копия пользователя
     */
    public User withDisplayName(String newName) {
        return new User(userId, newName, bio, balance, cardCount, createdAt, updatedAt);
    }

    /**
     * Возвращает копию пользователя с новым описанием профиля.
     *
     * @param newBio новое описание
     * @return копия пользователя
     */
    public User withBio(String newBio) {
        return new User(userId, displayName, newBio, balance, cardCount, createdAt, updatedAt);
    }
}