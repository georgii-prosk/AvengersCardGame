package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.UserCard;

import java.sql.Connection;
import java.util.List;

/**
 * Репозиторий для работы с карточками пользователей.
 */
public interface UserCardRepository {

    /**
     * Добавляет карточку пользователю (или увеличивает количество).
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     */
    void addCard(long userId, long heroId);

    /**
     * Добавляет карточку пользователю в рамках транзакции.
     *
     * @param conn   соединение, привязанное к транзакции
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     */
    void addCard(Connection conn, long userId, long heroId);

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    List<UserCard> findByUserId(long userId);

    /**
     * Считает общее количество карточек у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество карточек
     */
    int countByUserId(long userId);
}