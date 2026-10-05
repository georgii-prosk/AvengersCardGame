package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {

    private final DatabaseConfig dbConfig;

    public UserRepositoryImpl(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

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