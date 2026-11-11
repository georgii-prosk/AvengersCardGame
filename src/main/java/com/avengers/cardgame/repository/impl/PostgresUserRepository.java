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

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link UserRepository} на основе JDBC.
 */
public class PostgresUserRepository implements UserRepository {

    /** query-код нарушения уникальности в PostgreSQL. */
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

    /** Запрос для атомарного списания валюты. */
    private static final String SQL_SPEND_BALANCE =
            "UPDATE users SET balance = balance - ? WHERE telegram_id = ? AND balance >= ?";

    /** Запрос для атомарного списания кусочков. */
    private static final String SQL_SPEND_SHARDS =
            "UPDATE users SET shards = shards - ? WHERE telegram_id = ? AND shards >= ?";

    /** Запрос для атомарного начисления валюты. */
    private static final String SQL_ADD_BALANCE =
            "UPDATE users SET balance = balance + ? WHERE telegram_id = ?";

    /** Запрос для атомарного начисления кусочков. */
    private static final String SQL_ADD_SHARDS =
            "UPDATE users SET shards = shards + ? WHERE telegram_id = ?";

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

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresUserRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public @NotNull Optional<User> findByUserId(long userId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToUser(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding user", exception);
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
    public boolean spendBalance(@NotNull Connection connection, long userId, int amount) {
        return executeBalanceUpdate(connection, SQL_SPEND_BALANCE, userId, amount);
    }

    @Override
    public boolean spendShards(@NotNull Connection connection, long userId, int amount) {
        return executeBalanceUpdate(connection, SQL_SPEND_SHARDS, userId, amount);
    }

    @Override
    public boolean addBalance(@NotNull Connection connection, long userId, int amount) {
        requireNonNegative(amount);
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_BALANCE)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding balance", exception);
        }
    }

    @Override
    public boolean addShards(@NotNull Connection connection, long userId, int amount) {
        requireNonNegative(amount);
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_SHARDS)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Error adding shards", exception);
        }
    }

    /**
     * Выполняет атомарное списание с проверкой достаточности средств.
     *
     * @param connection соединение с активной транзакцией
     * @param query        query-запрос списания
     * @param userId     идентификатор пользователя
     * @param amount     сумма списания
     * @return {@code true}, если строка обновлена
     */
    private boolean executeBalanceUpdate(@NotNull Connection connection, String query, long userId, int amount) {
        requireNonNegative(amount);
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            statement.setInt(3, amount);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Error updating balance", exception);
        }
    }

    /**
     * Проверяет, что сумма операции неотрицательная.
     *
     * @param amount сумма операции
     * @throws IllegalArgumentException если сумма отрицательная
     */
    private void requireNonNegative(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative");
    }

    /**
     * Преобразует текущую строку {@link ResultSet} в объект {@link User}.
     *
     * @param resultSet результат query-запроса
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
     * @param resultSet       результат query-запроса
     * @param column   имя колонки
     * @return значение или {@code null}, если колонка содержит query NULL
     * @throws SQLException при ошибке чтения
     */
    private LocalDateTime toLocalDateTime(ResultSet resultSet, String column) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(column);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}