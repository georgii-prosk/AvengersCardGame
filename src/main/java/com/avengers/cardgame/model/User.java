package com.avengers.cardgame.model;

import java.time.LocalDateTime;

/**
 * Профиль игрока.
 *
 * @param userId      идентификатор пользователя на платформе
 * @param playerName  отображаемое имя
 * @param bio         описание профиля, может быть {@code null}
 * @param balance     баланс игровой валюты
 * @param cardCount   количество карточек в коллекции
 * @param createdAt   дата и время создания записи
 * @param updatedAt   дата и время последнего обновления записи
 */
public record User(
        long userId,
        PlayerName playerName,
        Bio bio,
        int balance,
        int cardCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Создаёт нового игрока с минимальными данными.
     *
     * @param userId     идентификатор пользователя
     * @param playerName отображаемое имя
     */
    public User(long userId, PlayerName playerName) {
        this(userId, playerName, null, 0, 0, null, null);
    }

    /**
     * Возвращает копию пользователя с новым отображаемым именем.
     *
     * @param newName новое имя
     * @return копия пользователя
     */
    public User withPlayerName(PlayerName newName) {
        return new User(userId, newName, bio, balance, cardCount, createdAt, updatedAt);
    }

    /**
     * Возвращает копию пользователя с новым описанием профиля.
     *
     * @param newBio новое описание
     * @return копия пользователя
     */
    public User withBio(Bio newBio) {
        return new User(userId, playerName, newBio, balance, cardCount, createdAt, updatedAt);
    }
}