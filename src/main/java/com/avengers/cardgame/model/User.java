package com.avengers.cardgame.model;

import java.time.LocalDateTime;

/**
 * Профиль игрока.
 *
 * @param userId          идентификатор пользователя на платформе
 * @param playerName      отображаемое имя
 * @param bio             описание профиля, может быть {@code null}
 * @param balance         баланс игровой валюты
 * @param cardCount       количество карточек в коллекции
 * @param createdAt       дата и время создания записи
 * @param updatedAt       дата и время последнего обновления записи
 * @param lastDailyAt     дата и время последнего получения /daily
 * @param dailyNotifiedAt дата и время последнего уведомления о готовности /daily
 */
public record User(
        long userId,
        PlayerName playerName,
        Bio bio,
        int balance,
        int cardCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastDailyAt,
        LocalDateTime dailyNotifiedAt
) {

    /**
     * Создаёт нового игрока с минимальными данными.
     *
     * @param userId     идентификатор пользователя
     * @param playerName отображаемое имя
     */
    public User(long userId, PlayerName playerName) {
        this(userId, playerName, null, 0, 0, null, null, null, null);
    }

    /**
     * Возвращает копию с новым именем.
     *
     * @param newName новое имя
     * @return копия
     */
    public User withPlayerName(PlayerName newName) {
        return new User(userId, newName, bio, balance, cardCount, createdAt, updatedAt, lastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с новым описанием.
     *
     * @param newBio новое описание
     * @return копия
     */
    public User withBio(Bio newBio) {
        return new User(userId, playerName, newBio, balance, cardCount, createdAt, updatedAt, lastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с обновлённым временем получения /daily.
     *
     * @param newLastDailyAt новое время
     * @return копия
     */
    public User withLastDailyAt(LocalDateTime newLastDailyAt) {
        return new User(userId, playerName, bio, balance, cardCount, createdAt, updatedAt, newLastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с обновлённым временем уведомления.
     *
     * @param newNotifiedAt новое время
     * @return копия
     */
    public User withDailyNotifiedAt(LocalDateTime newNotifiedAt) {
        return new User(userId, playerName, bio, balance, cardCount, createdAt, updatedAt, lastDailyAt, newNotifiedAt);
    }
}