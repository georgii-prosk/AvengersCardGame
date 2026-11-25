package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.model.UserCard;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO для работы с таблицей {@code user_cards}.
 */
final class UserCardDao {

    /** Запрос для добавления карточки (UPSERT). */
    private static final String SQL_ADD_CARD = """
            INSERT INTO user_cards (user_id, hero_id, count)
            VALUES (?, ?, 1)
            ON CONFLICT (user_id, hero_id) DO UPDATE SET
                count = user_cards.count + 1
            """;

    /** Запрос для уменьшения количества копий карточки. */
    private static final String SQL_REMOVE_CARD = """
            UPDATE user_cards
            SET count = count - ?
            WHERE user_id = ? AND hero_id = ? AND count >= ?
            """;

    /** Запрос для удаления опустевшей строки карточки. */
    private static final String SQL_DELETE_EMPTY_CARD =
            "DELETE FROM user_cards WHERE user_id = ? AND hero_id = ? AND count = 0";

    /** Запрос для подсчёта количества копий конкретной карточки. */
    private static final String SQL_COUNT_CARD =
            "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ? AND hero_id = ?";

    /** Запрос для подсчёта всех карточек пользователя. */
    private static final String SQL_COUNT_BY_USER =
            "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ?";

    /** Запрос для поиска карточек пользователя. */
    private static final String SQL_FIND_BY_USER =
            "SELECT user_id, hero_id, count FROM user_cards WHERE user_id = ?";

    /**
     * Добавляет карточку пользователю (или увеличивает количество).
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @throws SQLException при ошибке выполнения запроса
     */
    void addCard(Connection connection, long userId, long heroId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            statement.executeUpdate();
        }
    }

    /**
     * Удаляет указанное количество копий карточки.
     * <p>
     * При достижении нулевого количества строка удаляется.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @param count      количество копий для удаления
     * @return количество удалённых копий; {@code 0}, если копий недостаточно
     * @throws SQLException при ошибке выполнения запроса
     */
    int removeCard(Connection connection, long userId, long heroId, int count) throws SQLException {
        int updatedRows;
        try (PreparedStatement statement = connection.prepareStatement(SQL_REMOVE_CARD)) {
            statement.setInt(1, count);
            statement.setLong(2, userId);
            statement.setLong(3, heroId);
            statement.setInt(4, count);
            updatedRows = statement.executeUpdate();
        }
        if (updatedRows == 0) {
            return 0;
        }
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EMPTY_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            statement.executeUpdate();
        }
        return updatedRows;
    }

    /**
     * Считает количество копий карточки у пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @return количество копий карточки
     * @throws SQLException при ошибке выполнения запроса
     */
    int countCards(Connection connection, long userId, long heroId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    /**
     * Считает количество всех карточек пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @return количество карточек
     * @throws SQLException при ошибке выполнения запроса
     */
    int countByUserId(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        }
    }

    /**
     * Возвращает все карточки пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @return список карточек
     * @throws SQLException при ошибке выполнения запроса
     */
    List<UserCard> findByUserId(Connection connection, long userId) throws SQLException {
        List<UserCard> cards = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    cards.add(new UserCard(
                            resultSet.getLong("user_id"),
                            resultSet.getLong("hero_id"),
                            resultSet.getInt("count")
                    ));
                }
            }
        }
        return cards;
    }
}
