package com.avengers.cardgame.model;

import java.time.LocalDateTime;

public class User {

    private long telegramId;
    private String displayName;
    private String bio;
    private int balance;
    private int cardCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(long telegramId, String displayName) {
        this.telegramId = telegramId;
        this.displayName = displayName;
        this.bio = "";
        this.balance = 0;
        this.cardCount = 0;
    }

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

    public long getTelegramId() {
        return telegramId;
    }

    public void setTelegramId(long telegramId) {
        this.telegramId = telegramId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCardCount() {
        return cardCount;
    }

    public void setCardCount(int cardCount) {
        this.cardCount = cardCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

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