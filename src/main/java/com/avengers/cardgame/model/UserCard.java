package com.avengers.cardgame.model;

/**
 * Карточка героя в коллекции пользователя.
 *
 * @param userId идентификатор пользователя
 * @param heroId идентификатор героя
 * @param count  количество копий этой карточки
 */
public record UserCard(long userId, long heroId, int count) {
    /**
     * Создаёт запись о карточке в коллекции.
     *
     * @throws IllegalArgumentException если count отрицательный
     */
    public UserCard {
        if (count < 0) {
            throw new IllegalArgumentException("Count must be non-negative");
        }
    }
}