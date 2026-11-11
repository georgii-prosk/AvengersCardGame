package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.UserCard;
import org.jetbrains.annotations.NotNull;

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
     * Добавляет карточку пользователю в рамках переданного соединения.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     */
    void addCard(@NotNull Connection connection, long userId, long heroId);

    /**
     * Удаляет одну копию карточки у пользователя.
     * <p>
     * Если после удаления копий не остаётся, строка удаляется из таблицы.
     *
     * @param connection соединение с активной транзакцией
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @return {@code true}, если карточка была удалена; {@code false}, если её нет
     */
    boolean removeCard(@NotNull Connection connection, long userId, long heroId);

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    @NotNull List<UserCard> findByUserId(long userId);

    /**
     * Считает общее количество карточек у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество карточек
     */
    @SuppressWarnings("unused")
    int countByUserId(long userId);
}
