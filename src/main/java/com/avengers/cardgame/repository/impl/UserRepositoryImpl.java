package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

import java.sql.*;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 * <p>
 * Выполняет операции чтения и записи пользователей в таблице {@code users}.
 * Соединение с БД получается через {@link DatabaseConfig}.
 * <p>
 * Все методы при ошибках SQL оборачивают {@link SQLException} в
 * {@link RuntimeException}.
 */
public class UserRepositoryImpl implements UserRepository {

    /** Конфигурация подключения к базе данных. */
    private final DatabaseConfig dbConfig;

    /**
     * Создаёт репозиторий пользователей.
     *
     * @param dbConfig конфигурация подключения к БД
     */
    public UserRepositoryImpl(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException при ошибке выполнения SQL-запроса
     */
    @Override
    public Optional<User> findByTelegramId(long telegramId) {
        String sql = "SELECT * FROM users WHERE telegram_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, telegramId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRowToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user", e);
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Вставляет новую запись в таблицу {@code users}, сохраняя
     * {@code telegram_id}, {@code display_name} и {@code bio}.
     *
     * @throws RuntimeException при ошибке выполнения SQL-запроса
     */
    @Override
    public void save(User user) {
        String sql = "INSERT INTO users (telegram_id, display_name, bio) VALUES (?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getTelegramId());
            stmt.setString(2, user.getDisplayName());
            stmt.setString(3, user.getBio());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Обновляет {@code display_name} и {@code bio} по {@code telegram_id}.
     *
     * @throws RuntimeException при ошибке выполнения SQL-запроса
     */
    @Override
    public void update(User user) {
        String sql = "UPDATE users SET display_name = ?, bio = ? WHERE telegram_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getDisplayName());
            stmt.setString(2, user.getBio());
            stmt.setLong(3, user.getTelegramId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    /**
     * Преобразует текущую строку {@link ResultSet} в объект {@link User}.
     *
     * @param rs результат SQL-запроса, позиционированный на строке с данными
     * @return заполненный объект {@link User}
     * @throws SQLException при ошибке чтения полей из {@link ResultSet}
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