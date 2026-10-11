package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 */
public class UserRepositoryImpl implements UserRepository {

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM users WHERE telegram_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO users (telegram_id, display_name, bio) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE users SET display_name = ?, bio = ? WHERE telegram_id = ?";

    private final ConnectionFactory connectionFactory;

    public UserRepositoryImpl(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }


    /**
     * Находит пользователя по идентификатору.
     *
     * @param userId идентификатор
     * @return пользователь, если найден
     */
    @Override
    public Optional<User> findByUserId(long userId) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
        return Optional.empty();
    }


    /**
     * Сохраняет нового пользователя.
     *
     * @param user пользователь
     */
    @Override
    public void save(User user) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setLong(1, user.userId());
            stmt.setString(2, user.displayName());
            stmt.setString(3, user.bio());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }


    /**
     * Обновляет данные пользователя.
     *
     * @param user пользователь с новыми данными
     */
    @Override
    public void update(User user) {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, user.displayName());
            stmt.setString(2, user.bio());
            stmt.setLong(3, user.userId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    /**
     * Преобразует текущую строку {@link ResultSet} в объект {@link User}.
     *
     * @param rs результат SQL-запроса
     * @return заполненный объект
     * @throws SQLException при ошибке чтения полей
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("telegram_id"),
                rs.getString("display_name"),
                rs.getString("bio"),
                rs.getInt("balance"),
                rs.getInt("card_count"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}