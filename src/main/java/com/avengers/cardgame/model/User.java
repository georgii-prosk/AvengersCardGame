package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidUserException;

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
 * @param lastDailyAt     дата и время последнего получения /daily, {@code null} если ещё не получал
 * @param dailyNotifiedAt дата и время последнего уведомления о готовности /daily, {@code null} если не отправлялось
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
     * Создаёт профиль с проверкой инвариантов.
     * <p>
     * Предпочтительнее использовать {@link #of(long, PlayerName, Bio, int, int, LocalDateTime, LocalDateTime, LocalDateTime, LocalDateTime)}.
     *
     * @throws IllegalArgumentException если баланс или количество карточек отрицательные,
     *                                  либо время обновления раньше времени создания
     */
    public User {
        if (balance < 0) {
            throw new IllegalArgumentException("Balance must be non-negative");
        }
        if (cardCount < 0) {
            throw new IllegalArgumentException("Card count must be non-negative");
        }
        if (createdAt != null && updatedAt != null && updatedAt.isBefore(createdAt)) {
            throw new IllegalArgumentException("UpdatedAt cannot be before createdAt");
        }
    }

    /**
     * Создаёт профиль с проверкой инвариантов.
     *
     * @throws InvalidUserException если данные некорректны
     */
    public static User of(
            long userId, PlayerName playerName, Bio bio,
            int balance, int cardCount,
            LocalDateTime createdAt, LocalDateTime updatedAt,
            LocalDateTime lastDailyAt, LocalDateTime dailyNotifiedAt
    ) throws InvalidUserException {
        try {
            return new User(userId, playerName, bio, balance, cardCount,
                    createdAt, updatedAt, lastDailyAt, dailyNotifiedAt);
        } catch (IllegalArgumentException e) {
            throw new InvalidUserException(e.getMessage());
        }
    }

    /**
     * Создаёт профиль без данных о /daily.
     *
     * @throws InvalidUserException если данные некорректны
     */
    public static User of(
            long userId, PlayerName playerName, Bio bio,
            int balance, int cardCount,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) throws InvalidUserException {
        return of(userId, playerName, bio, balance, cardCount,
                createdAt, updatedAt, null, null);
    }

    /**
     * Создаёт профиль с семью полями; данные о /daily пустые.
     *
     * @throws IllegalArgumentException если данные некорректны
     */
    public User(
            long userId, PlayerName playerName, Bio bio,
            int balance, int cardCount,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {
        this(userId, playerName, bio, balance, cardCount,
                createdAt, updatedAt, null, null);
    }

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
        return new User(userId, newName, bio, balance, cardCount,
                createdAt, updatedAt, lastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с новым описанием.
     *
     * @param newBio новое описание
     * @return копия
     */
    public User withBio(Bio newBio) {
        return new User(userId, playerName, newBio, balance, cardCount,
                createdAt, updatedAt, lastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с новым временем последнего получения /daily.
     *
     * @param newLastDailyAt новое время
     * @return копия
     */
    public User withLastDailyAt(LocalDateTime newLastDailyAt) {
        return new User(userId, playerName, bio, balance, cardCount,
                createdAt, updatedAt, newLastDailyAt, dailyNotifiedAt);
    }

    /**
     * Возвращает копию с новым временем последнего уведомления о готовности /daily.
     *
     * @param newDailyNotifiedAt новое время
     * @return копия
     */
    public User withDailyNotifiedAt(LocalDateTime newDailyNotifiedAt) {
        return new User(userId, playerName, bio, balance, cardCount,
                createdAt, updatedAt, lastDailyAt, newDailyNotifiedAt);
    }
}