package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 */
public class PostgresUserRepository extends JdbcSupport implements UserRepository {

    /** SQL-код нарушения уникальности в PostgreSQL. */
    private static final String UNIQUE_VIOLATION_SQL_STATE = "23505";

    /** Запрос для поиска пользователя по идентификатору с вычислением количества карточек. */
    private static final String SQL_FIND_BY_ID = """
            SELECT telegram_id, display_name, bio, balance, shards,
                   (SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_cards.user_id = users.telegram_id) AS card_count,
                   created_at, updated_at, last_daily_at, daily_notified_at
            FROM users WHERE telegram_id = ?
            """;

    /** Запрос для вставки нового пользователя. */
    private static final String SQL_INSERT =
            "INSERT INTO users (telegram_id, display_name, bio) VALUES (?, ?, ?)";

    /** Запрос для обновления отображаемого имени и описания профиля. */
    private static final String SQL_UPDATE =
            "UPDATE users SET display_name = ?, bio = ? WHERE telegram_id = ?";

    /** Запрос для обновления времени последнего получения ежедневной награды. */
    private static final String SQL_UPDATE_LAST_DAILY =
            "UPDATE users SET last_daily_at = ? WHERE telegram_id = ?";

    /** Запрос для обновления времени последнего уведомления о готовности награды. */
    private static final String SQL_UPDATE_DAILY_NOTIFIED =
            "UPDATE users SET daily_notified_at = ? WHERE telegram_id = ?";

    /** Запрос для поиска пользователей, которым пора отправить уведомление о награде. */
    private static final String SQL_FIND_USERS_TO_NOTIFY = """
            SELECT telegram_id, display_name, bio, balance, shards,
                   (SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_cards.user_id = users.telegram_id) AS card_count,
                   created_at, updated_at, last_daily_at, daily_notified_at
            FROM users
            WHERE last_daily_at IS NOT NULL
              AND last_daily_at <= ?
              AND (daily_notified_at IS NULL OR daily_notified_at < last_daily_at)
            """;

    /** Запрос для атомарного списания валюты. */
    private static final String SQL_SPEND_BALANCE =
            "UPDATE users SET balance = balance - ? WHERE telegram_id = ? AND balance >= ?";

    /** Запрос для атомарного списания кусочков. */
    private static final String SQL_SPEND_SHARDS =
            "UPDATE users SET shards = shards - ? WHERE telegram_id = ? AND shards >= ?";

    /** Запрос для начисления валюты. */
    private static final String SQL_ADD_BALANCE =
            "UPDATE users SET balance = balance + ? WHERE telegram_id = ?";

    /** Запрос для начисления кусочков. */
    private static final String SQL_ADD_SHARDS =
            "UPDATE users SET shards = shards + ? WHERE telegram_id = ?";

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresUserRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public @NotNull Optional<User> findByUserId(long userId) {
        return findByUserId(null, userId);
    }

    @Override
    public @NotNull Optional<User> findByUserId(@Nullable Connection connection, long userId) {
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_FIND_BY_ID)) {
                statement.setLong(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToUser(resultSet));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding user", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
        return Optional.empty();
    }

    @Override
    public void save(@NotNull User user) throws UserAlreadyExistsException {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setLong(1, user.userId());
            statement.setString(2, user.playerName().value());
            statement.setString(3, user.bio() == null ? null : user.bio().value());
            statement.executeUpdate();
        } catch (SQLException exception) {
            if (UNIQUE_VIOLATION_SQL_STATE.equals(exception.getSQLState())) {
                throw new UserAlreadyExistsException(user.userId());
            }
            throw new RuntimeException("Error saving user", exception);
        }
    }

    @Override
    public void update(@NotNull User user) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, user.playerName().value());
            statement.setString(2, user.bio() == null ? null : user.bio().value());
            statement.setLong(3, user.userId());

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(user.userId());
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating user", exception);
        }
    }

    @Override
    public void updateLastDailyAt(long userId, @NotNull LocalDateTime lastDailyAt) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_LAST_DAILY)) {
            statement.setTimestamp(1, Timestamp.valueOf(lastDailyAt));
            statement.setLong(2, userId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating last daily time", exception);
        }
    }

    @Override
    public @NotNull List<User> findUsersToNotify(@NotNull LocalDateTime thresholdTime) {
        List<User> users = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_USERS_TO_NOTIFY)) {
            statement.setTimestamp(1, Timestamp.valueOf(thresholdTime));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRowToUser(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding users to notify", exception);
        }
        return users;
    }

    @Override
    public void updateDailyNotifiedAt(long userId, @NotNull LocalDateTime notifiedAt) throws UserNotFoundException {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_DAILY_NOTIFIED)) {
            statement.setTimestamp(1, Timestamp.valueOf(notifiedAt));
            statement.setLong(2, userId);

            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating daily notified time", exception);
        }
    }

    @Override
    public boolean spendBalance(@Nullable Connection connection, long userId, int amount) {
        return spendAmount(connection, SQL_SPEND_BALANCE, userId, amount);
    }

    @Override
    public boolean spendShards(@Nullable Connection connection, long userId, int amount) {
        return spendAmount(connection, SQL_SPEND_SHARDS, userId, amount);
    }

    @Override
    public void addBalance(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException {
        addAmount(connection, SQL_ADD_BALANCE, userId, amount);
    }

    @Override
    public void addShards(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException {
        addAmount(connection, SQL_ADD_SHARDS, userId, amount);
    }

    /**
     * Атомарно списывает указанное количество единиц ресурса.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param sql        SQL-запрос списания
     * @param userId     идентификатор пользователя
     * @param amount     количество для списания
     * @return {@code true}, если списание выполнено
     */
    private boolean spendAmount(@Nullable Connection connection, String sql, long userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive");
        }
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(sql)) {
                statement.setInt(1, amount);
                statement.setLong(2, userId);
                statement.setInt(3, amount);
                return statement.executeUpdate() > 0;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error spending resource", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
    }

    /**
     * Начисляет указанное количество единиц ресурса.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param sql        SQL-запрос начисления
     * @param userId     идентификатор пользователя
     * @param amount     количество для начисления
     * @throws UserNotFoundException если пользователь не найден
     */
    private void addAmount(@Nullable Connection connection, String sql, long userId, int amount)
            throws UserNotFoundException {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add must be non-negative");
        }
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(sql)) {
                statement.setInt(1, amount);
                statement.setLong(2, userId);
                if (statement.executeUpdate() == 0) {
                    throw new UserNotFoundException(userId);
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding resource", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
    }

    /**
     * Преобразует текущую строку {@link ResultSet} в объект {@link User}.
     *
     * @param resultSet результат SQL-запроса
     * @return заполненный объект
     * @throws SQLException при ошибке чтения полей
     */
    private User mapRowToUser(ResultSet resultSet) throws SQLException {
        String displayNameValue = resultSet.getString("display_name");
        String bioValue = resultSet.getString("bio");

        try {
            return User.of(
                    resultSet.getLong("telegram_id"),
                    PlayerName.of(displayNameValue),
                    bioValue == null || bioValue.isEmpty() ? null : Bio.of(bioValue),
                    resultSet.getInt("balance"),
                    resultSet.getInt("shards"),
                    resultSet.getInt("card_count"),
                    toLocalDateTime(resultSet, "created_at"),
                    toLocalDateTime(resultSet, "updated_at"),
                    toLocalDateTime(resultSet, "last_daily_at"),
                    toLocalDateTime(resultSet, "daily_notified_at")
            );
        } catch (AppException exception) {
            throw new RuntimeException("Corrupted user data in DB for ID " + resultSet.getLong("telegram_id"), exception);
        }
    }

    /**
     * Читает {@link Timestamp} из указанной колонки и приводит к {@link LocalDateTime}.
     *
     * @param resultSet результат SQL-запроса
     * @param column    имя колонки
     * @return значение или {@code null}, если колонка содержит SQL NULL
     * @throws SQLException при ошибке чтения
     */
    private LocalDateTime toLocalDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
