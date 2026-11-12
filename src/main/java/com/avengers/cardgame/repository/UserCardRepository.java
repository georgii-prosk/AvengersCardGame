package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.UserCard;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * Добавляет карточку пользователю в рамках заданного соединения.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     */
    void addCard(@Nullable Connection connection, long userId, long heroId);

    /**
     * Удаляет указанное количество копий карточки в рамках заданного соединения.
     * <p>
     * При достижении нулевого количества строка удаляется из хранилища.
     * <p>
     * Метод вызывается только в инвертированном виде: результат {@code false}
     * означает, что копий недостаточно.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @param count      количество копий для удаления
     * @return {@code true}, если удаление выполнено; {@code false}, если копий недостаточно
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean removeCard(@Nullable Connection connection, long userId, long heroId, int count);

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    @NotNull List<UserCard> findByUserId(long userId);

    /**
     * Считает количество копий конкретной карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий карточки
     */
    int countByUserIdAndHeroId(long userId, long heroId);
}
