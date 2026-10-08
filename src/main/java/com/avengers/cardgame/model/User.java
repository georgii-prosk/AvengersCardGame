package com.avengers.cardgame.model;

import java.time.LocalDateTime;

/**
 * Модель пользователя (игрока) в карточной игре "Avengers".
 * <p>
 * Содержит основную информацию о профиле: идентификатор в Telegram,
 * отображаемое имя, описание, баланс игровой валюты («щиты»), количество
 * карточек в коллекции и метки времени создания/обновления записи.
 */
public class User {

    /** Идентификатор пользователя в Telegram. */
    private long telegramId;

    /** Отображаемое имя пользователя. */
    private String displayName;

    /** Краткое описание профиля. */
    private String bio;

    /** Баланс игровой валюты («щиты»). */
    private int balance;

    /** Количество карточек в коллекции пользователя. */
    private int cardCount;

    /** Дата и время создания записи. */
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления записи. */
    private LocalDateTime updatedAt;

    /**
     * Пустой конструктор для фреймворков (ORM, десериализация).
     */
    public User() {
    }

    /**
     * Создаёт нового пользователя с минимальными данными.
     * <p>
     * Поля {@code bio}, {@code balance} и {@code cardCount} инициализируются
     * значениями по умолчанию ({@code ""}, {@code 0}, {@code 0}).
     *
     * @param telegramId  идентификатор пользователя в Telegram
     * @param displayName отображаемое имя
     */
    public User(long telegramId, String displayName) {
        this.telegramId = telegramId;
        this.displayName = displayName;
        this.bio = "";
        this.balance = 0;
        this.cardCount = 0;
    }

    /**
     * Создаёт пользователя со всеми полями.
     *
     * @param telegramId  идентификатор пользователя в Telegram
     * @param displayName отображаемое имя
     * @param bio         описание профиля
     * @param balance     баланс игровой валюты
     * @param cardCount   количество карточек в коллекции
     * @param createdAt   дата и время создания записи
     * @param updatedAt   дата и время последнего обновления записи
     */
    public User(long telegramId, String displayName, String bio,
                int balance, int cardCount,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.telegramId = telegramId;
        this.displayName = displayName;
        this.bio = bio;
        this.balance = balance;
        this.cardCount = cardCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * @return идентификатор пользователя в Telegram
     */
    public long getTelegramId() {
        return telegramId;
    }

    /**
     * @param telegramId идентификатор пользователя в Telegram
     */
    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    /**
     * @return отображаемое имя пользователя
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName отображаемое имя пользователя
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return описание профиля
     */
    public String getBio() {
        return bio;
    }

    /**
     * @param bio описание профиля
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * @return баланс игровой валюты («щиты»)
     */
    public int getBalance() {
        return balance;
    }

    /**
     * @param balance баланс игровой валюты («щиты»)
     */
    public void setBalance(int balance) {
        this.balance = balance;
    }

    /**
     * @return количество карточек в коллекции
     */
    public int getCardCount() {
        return cardCount;
    }

    /**
     * @param cardCount количество карточек в коллекции
     */
    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    /**
     * @return дата и время создания записи
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt дата и время создания записи
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return дата и время последнего обновления записи
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * @param updatedAt дата и время последнего обновления записи
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * @return строковое представление пользователя
     */
    @Override
    public String toString() {
        return "User{" +
                "telegramId=" + telegramId +
                ", displayName='" + displayName + '\'' +
                ", bio='" + bio + '\'' +
                ", balance=" + balance +
                ", cardCount=" + cardCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}