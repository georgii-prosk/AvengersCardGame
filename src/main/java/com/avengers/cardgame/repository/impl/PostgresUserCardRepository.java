package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;

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

    /** Запрос для поиска карточек пользователя. */
    private static final String SQL_FIND_BY_USER = "SELECT * FROM user_cards WHERE user_id = ?";

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
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_ADD_CARD)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, heroId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding card", e);
        }
    }

    @Override
    public List<UserCard> findByUserId(long userId) {
        List<UserCard> cards = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_USER)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(new UserCard(
                            rs.getLong("user_id"),
                            rs.getLong("hero_id"),
                            rs.getInt("count")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding cards by user", e);
        }
        return cards;
    }

    @Override
    public int countByUserId(long userId) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_BY_USER)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting cards by user", e);
        }
        return 0;
    }
}