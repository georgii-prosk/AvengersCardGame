package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 */
public class PostgresUserRepository implements UserRepository {

    /** SQL-код нарушения уникальности в PostgreSQL. */
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    private static final String SQL_FIND_BY_ID =
            "SELECT * FROM users WHERE telegram_id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO users (telegram_id, display_name, bio) VALUES (?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE users SET display_name = ?, bio = ? WHERE telegram_id = ?";

    private static final String SQL_UPDATE_LAST_DAILY =
            "UPDATE users SET last_daily_at = ? WHERE telegram_id = ?";

    private static final String SQL_UPDATE_DAILY_NOTIFIED =
            "UPDATE users SET daily_notified_at = ? WHERE telegram_id = ?";

    private static final String SQL_FIND_USERS_TO_NOTIFY = """
            SELECT * FROM users
            WHERE last_daily_at IS NOT NULL
              AND last_daily_at <= ?
              AND (daily_notified_at IS NULL OR daily_notified_at < last_daily_at)
            """;

    private static final String SQL_FIND_BY_ID_FOR_UPDATE =
            "SELECT * FROM users WHERE telegram_id = ? FOR UPDATE";

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

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

    @Override
    public void save(User user) throws UserAlreadyExistsException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
            stmt.setLong(1, user.userId());
            stmt.setString(2, user.playerName().value());
            stmt.setString(3, user.bio() == null ? null : user.bio().value());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (UNIQUE_VIOLATION_SQL_STATE.equals(e.getSQLState())) {
                throw new UserAlreadyExistsException(user.userId());
            }
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public void update(User user) throws UserNotFoundException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {
            stmt.setString(1, user.playerName().value());
            stmt.setString(2, user.bio() == null ? null : user.bio().value());
            stmt.setLong(3, user.userId());

            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(user.userId());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    @Override
    public Optional<User> findByUserIdForUpdate(Connection conn, long userId) {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID_FOR_UPDATE)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error locking user", e);
        }
        return Optional.empty();
    }

    @Override
    public void updateLastDailyAt(Connection conn, long userId, LocalDateTime lastDailyAt)
            throws UserNotFoundException {
        try (PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_LAST_DAILY)) {
            stmt.setTimestamp(1, Timestamp.valueOf(lastDailyAt));
            stmt.setLong(2, userId);

            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last daily time", e);
        }
    }

    @Override
    public void updateLastDailyAt(long userId, LocalDateTime lastDailyAt) throws UserNotFoundException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_LAST_DAILY)) {
            stmt.setTimestamp(1, Timestamp.valueOf(lastDailyAt));
            stmt.setLong(2, userId);

            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating last daily time", e);
        }
    }

    @Override
    public List<User> findUsersToNotify(LocalDateTime thresholdTime) {
        List<User> users = new ArrayList<>();
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_USERS_TO_NOTIFY)) {
            stmt.setTimestamp(1, Timestamp.valueOf(thresholdTime));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding users to notify", e);
        }
        return users;
    }

    @Override
    public void updateDailyNotifiedAt(long userId, LocalDateTime notifiedAt) throws UserNotFoundException {
        try (Connection conn = connectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_DAILY_NOTIFIED)) {
            stmt.setTimestamp(1, Timestamp.valueOf(notifiedAt));
            stmt.setLong(2, userId);

            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating daily notified time", e);
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
        String displayNameValue = rs.getString("display_name");
        String bioValue = rs.getString("bio");

        try {
            return User.of(
                    rs.getLong("telegram_id"),
                    PlayerName.of(displayNameValue),
                    bioValue == null || bioValue.isEmpty() ? null : Bio.of(bioValue),
                    rs.getInt("balance"),
                    rs.getInt("card_count"),
                    toLocalDateTime(rs, "created_at"),
                    toLocalDateTime(rs, "updated_at"),
                    toLocalDateTime(rs, "last_daily_at"),
                    toLocalDateTime(rs, "daily_notified_at")
            );
        } catch (AppException e) {
            // Если в БД лежат битые данные, выбрасываем техническую ошибку
            throw new RuntimeException("Corrupted user data in DB for ID " + rs.getLong("telegram_id"), e);
        }
    }

    /**
     * Читает {@link Timestamp} из указанной колонки и приводит к {@link LocalDateTime}.
     *
     * @param rs       результат SQL-запроса
     * @param column   имя колонки
     * @return значение или {@code null}, если колонка содержит SQL NULL
     * @throws SQLException при ошибке чтения
     */
    private LocalDateTime toLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }
}