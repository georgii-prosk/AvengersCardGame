package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация {@link UserCardRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresUserCardRepository implements UserCardRepository {

    /** Запрос для добавления карточки (UPSERT). */
    private static final String SQL_ADD_CARD = """
            INSERT INTO user_cards (user_id, hero_id, count)
            VALUES (?, ?, 1)
            ON CONFLICT (user_id, hero_id) DO UPDATE SET
                count = user_cards.count + 1
            """;

    /** Запрос для уменьшения количества копий карточки. */
    private static final String SQL_DECREASE_CARD =
            "UPDATE user_cards SET count = count - 1 WHERE user_id = ? AND hero_id = ? AND count > 0";

    /** Запрос для удаления опустевшей строки карточки. */
    private static final String SQL_DELETE_EMPTY_CARD =
            "DELETE FROM user_cards WHERE user_id = ? AND hero_id = ? AND count = 0";

    /** Запрос для поиска карточек пользователя. */
    private static final String SQL_FIND_BY_USER =
            "SELECT user_id, hero_id, count FROM user_cards WHERE user_id = ?";

    /** Запрос для подсчёта общего количества карточек. */
    private static final String SQL_COUNT_BY_USER = "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ?";

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresUserCardRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void addCard(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection()) {
            addCard(connection, userId, heroId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding card", exception);
        }
    }

    @Override
    public void addCard(@NotNull Connection connection, long userId, long heroId) {
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding card", exception);
        }
    }

    @Override
    public boolean removeCard(@NotNull Connection connection, long userId, long heroId) {
        try {
            int updatedRows;
            try (PreparedStatement statement = connection.prepareStatement(SQL_DECREASE_CARD)) {
                statement.setLong(1, userId);
                statement.setLong(2, heroId);
                updatedRows = statement.executeUpdate();
            }
            if (updatedRows == 0) {
                return false;
            }
            try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EMPTY_CARD)) {
                statement.setLong(1, userId);
                statement.setLong(2, heroId);
                statement.executeUpdate();
            }
            return true;
        } catch (SQLException exception) {
            throw new RuntimeException("Error removing card", exception);
        }
    }

    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        List<UserCard> cards = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_USER)) {
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
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding cards by user", exception);
        }
        return cards;
    }

    @Override
    public int countByUserId(long userId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT_BY_USER)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error counting cards by user", exception);
        }
        return 0;
    }
}
