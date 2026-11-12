package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация {@link UserCardRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresUserCardRepository extends JdbcSupport implements UserCardRepository {

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

    /** Запрос для поиска карточек пользователя. */
    private static final String SQL_FIND_BY_USER =
            "SELECT user_id, hero_id, count FROM user_cards WHERE user_id = ?";

    /** Запрос для подсчёта количества копий конкретной карточки. */
    private static final String SQL_COUNT_BY_USER_AND_HERO =
            "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ? AND hero_id = ?";

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresUserCardRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public void addCard(long userId, long heroId) {
        addCard(null, userId, heroId);
    }

    @Override
    public void addCard(@Nullable Connection connection, long userId, long heroId) {
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_ADD_CARD)) {
                statement.setLong(1, userId);
                statement.setLong(2, heroId);
                statement.executeUpdate();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding card", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
    }

    @Override
    public boolean removeCard(@Nullable Connection connection, long userId, long heroId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to remove must be positive");
        }
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_REMOVE_CARD)) {
                statement.setInt(1, count);
                statement.setLong(2, userId);
                statement.setLong(3, heroId);
                statement.setInt(4, count);
                if (statement.executeUpdate() == 0) {
                    return false;
                }
            }
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_DELETE_EMPTY_CARD)) {
                statement.setLong(1, userId);
                statement.setLong(2, heroId);
                statement.executeUpdate();
            }
            return true;
        } catch (SQLException exception) {
            throw new RuntimeException("Error removing card", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
    }

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        List<UserCard> cards = new ArrayList<>();
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(null);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_FIND_BY_USER)) {
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
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding cards by user", exception);
        } finally {
            closeIfOwned(null, actualConnection);
        }
        return cards;
    }

    /**
     * Считает количество копий конкретной карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий карточки
     */
    @Override
    public int countByUserIdAndHeroId(long userId, long heroId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT_BY_USER_AND_HERO)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error counting cards by user and hero", exception);
        }
        return 0;
    }
}