package com.avengers.cardgame.model;

import java.time.LocalDateTime;

/**
 * Строитель для создания объектов {@link User}.
 * <p>
 * Позволяет избежать передачи 9 аргументов в конструктор и делает код
 * создания пользователя более читаемым.
 */
public class UserBuilder {

    /** Идентификатор пользователя. */
    private long userId;

    /** Отображаемое имя. */
    private PlayerName playerName;

    /** Описание профиля. */
    private Bio bio;

    /** Баланс игровой валюты. */
    private int balance;

    /** Количество карточек в коллекции. */
    private int cardCount;

    /** Дата и время создания записи. */
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи. */
    private LocalDateTime updatedAt;

    /** Дата и время последнего получения /daily. */
    private LocalDateTime lastDailyAt;

    /** Дата и время последнего уведомления о готовности /daily. */
    private LocalDateTime dailyNotifiedAt;

    /**
     * Создаёт пустой строитель. Все поля по умолчанию: числа равны 0,
     * объекты и даты равны {@code null}.
     */
    public UserBuilder() {
    }

    /**
     * Задаёт идентификатор пользователя.
     *
     * @param userId идентификатор
     * @return этот же строитель
     */
    public UserBuilder userId(long userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Задаёт отображаемое имя.
     *
     * @param playerName имя
     * @return этот же строитель
     */
    public UserBuilder playerName(PlayerName playerName) {
        this.playerName = playerName;
        return this;
    }

    /**
     * Задаёт описание профиля.
     *
     * @param bio описание
     * @return этот же строитель
     */
    public UserBuilder bio(Bio bio) {
        this.bio = bio;
        return this;
    }

    /**
     * Задаёт баланс игровой валюты.
     *
     * @param balance баланс
     * @return этот же строитель
     */
    public UserBuilder balance(int balance) {
        this.balance = balance;
        return this;
    }

    /**
     * Задаёт количество карточек.
     *
     * @param cardCount количество
     * @return этот же строитель
     */
    public UserBuilder cardCount(int cardCount) {
        this.cardCount = cardCount;
        return this;
    }

    /**
     * Задаёт дату и время создания.
     *
     * @param createdAt дата и время
     * @return этот же строитель
     */
    public UserBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    /**
     * Задаёт дату и время обновления.
     *
     * @param updatedAt дата и время
     * @return этот же строитель
     */
    public UserBuilder updatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    /**
     * Задаёт дату и время последнего получения /daily.
     *
     * @param lastDailyAt дата и время
     * @return этот же строитель
     */
    public UserBuilder lastDailyAt(LocalDateTime lastDailyAt) {
        this.lastDailyAt = lastDailyAt;
        return this;
    }

    /**
     * Задаёт дату и время последнего уведомления.
     *
     * @param dailyNotifiedAt дата и время
     * @return этот же строитель
     */
    public UserBuilder dailyNotifiedAt(LocalDateTime dailyNotifiedAt) {
        this.dailyNotifiedAt = dailyNotifiedAt;
        return this;
    }

    /**
     * Создаёт объект пользователя с заданными параметрами.
     *
     * @return новый пользователь
     */
    public User build() {
        return new User(
                userId, playerName, bio, balance, cardCount,
                createdAt, updatedAt, lastDailyAt, dailyNotifiedAt
        );
    }
}