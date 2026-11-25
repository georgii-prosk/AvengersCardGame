package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.Bio;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * DAO для работы с таблицей {@code users}.
 */
final class UserDao {

    /** Запрос для поиска пользователя по идентификатору. */
    private static final String SQL_FIND_BY_ID = """
            SELECT telegram_id, display_name, bio, balance, shards,
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
                   created_at, updated_at, last_daily_at, daily_notified_at
            FROM users
            WHERE last_daily_at IS NOT NULL
              AND last_daily_at <= ?
              AND (daily_notified_at IS NULL OR daily_notified_at < last_daily_at)
            """;

    /** Запрос для списания валюты с возвратом нового баланса. */
    private static final String SQL_SPEND_BALANCE = """
            UPDATE users SET balance = balance - ?
            WHERE telegram_id = ? AND balance >= ?
            RETURNING balance
            """;

    /** Запрос для списания кусочков с возвратом нового баланса. */
    private static final String SQL_SPEND_SHARDS = """
            UPDATE users SET shards = shards - ?
            WHERE telegram_id = ? AND shards >= ?
            RETURNING shards
            """;

    /** Запрос для начисления кусочков с возвратом нового баланса. */
    private static final String SQL_ADD_SHARDS = """
            UPDATE users SET shards = shards + ?
            WHERE telegram_id = ?
            RETURNING shards
            """;

    /** Запрос для начисления валюты. */
    private static final String SQL_ADD_BALANCE =
            "UPDATE users SET balance = balance + ? WHERE telegram_id = ?";

    /** Запрос для чтения баланса валюты. */
    private static final String SQL_FIND_BALANCE =
            "SELECT balance FROM users WHERE telegram_id = ?";

    /** Запрос для чтения баланса кусочков. */
    private static final String SQL_FIND_SHARDS =
            "SELECT shards FROM users WHERE telegram_id = ?";

    /**
     * Находит пользователя по идентификатору.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @return пользователь, если найден
     * @throws SQLException при ошибке выполнения запроса
     */
    Optional<User> findById(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRowToUser(resultSet)) : Optional.empty();
            }
        }
    }

    /**
     * Вставляет нового пользователя.
     *
     * @param connection соединение с БД
     * @param user       пользователь
     * @throws SQLException при ошибке выполнения запроса
     */
    void insert(Connection connection, User user) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setLong(1, user.userId());
            statement.setString(2, user.playerName().value());
            statement.setString(3, user.bio() == null ? null : user.bio().value());
            statement.executeUpdate();
        }
    }

    /**
     * Обновляет отображаемое имя и описание профиля.
     *
     * @param connection соединение с БД
     * @param user       пользователь
     * @return {@code true}, если пользователь найден; иначе {@code false}
     * @throws SQLException при ошибке выполнения запроса
     */
    boolean update(Connection connection, User user) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, user.playerName().value());
            statement.setString(2, user.bio() == null ? null : user.bio().value());
            statement.setLong(3, user.userId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Обновляет время последнего получения ежедневной награды.
     *
     * @param connection   соединение с БД
     * @param userId       идентификатор пользователя
     * @param lastDailyAt  время последней выдачи
     * @return {@code true}, если пользователь найден; иначе {@code false}
     * @throws SQLException при ошибке выполнения запроса
     */
    boolean updateLastDailyAt(Connection connection, long userId, LocalDateTime lastDailyAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_LAST_DAILY)) {
            statement.setTimestamp(1, Timestamp.valueOf(lastDailyAt));
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Находит пользователей, которым пора отправить уведомление о награде.
     *
     * @param connection    соединение с БД
     * @param thresholdTime пороговое время
     * @return список пользователей
     * @throws SQLException при ошибке выполнения запроса
     */
    List<User> findUsersToNotify(Connection connection, LocalDateTime thresholdTime) throws SQLException {
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_USERS_TO_NOTIFY)) {
            statement.setTimestamp(1, Timestamp.valueOf(thresholdTime));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapRowToUser(resultSet));
                }
            }
        }
        return users;
    }

    /**
     * Обновляет время последнего уведомления о готовности награды.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param notifiedAt время уведомления
     * @return {@code true}, если пользователь найден; иначе {@code false}
     * @throws SQLException при ошибке выполнения запроса
     */
    boolean updateDailyNotifiedAt(Connection connection, long userId, LocalDateTime notifiedAt) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_DAILY_NOTIFIED)) {
            statement.setTimestamp(1, Timestamp.valueOf(notifiedAt));
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Списывает валюту с баланса пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param amount     сумма списания
     * @return новый баланс, если списание выполнено
     * @throws SQLException при ошибке выполнения запроса
     */
    OptionalInt spendBalance(Connection connection, long userId, int amount) throws SQLException {
        return executeBalanceUpdate(connection, SQL_SPEND_BALANCE, amount, userId, amount);
    }

    /**
     * Списывает кусочки с баланса пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков
     * @return новый баланс, если списание выполнено
     * @throws SQLException при ошибке выполнения запроса
     */
    OptionalInt spendShards(Connection connection, long userId, int amount) throws SQLException {
        return executeBalanceUpdate(connection, SQL_SPEND_SHARDS, amount, userId, amount);
    }

    /**
     * Начисляет кусочки на баланс пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param amount     количество кусочков
     * @return новый баланс, если пользователь найден
     * @throws SQLException при ошибке выполнения запроса
     */
    OptionalInt addShards(Connection connection, long userId, int amount) throws SQLException {
        return executeBalanceUpdate(connection, SQL_ADD_SHARDS, amount, userId);
    }

    /**
     * Начисляет валюту на баланс пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param amount     сумма начисления
     * @return {@code true}, если пользователь найден; иначе {@code false}
     * @throws SQLException при ошибке выполнения запроса
     */
    boolean addBalance(Connection connection, long userId, int amount) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_BALANCE)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Читает баланс валюты пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @return баланс, если пользователь найден
     * @throws SQLException при ошибке выполнения запроса
     */
    OptionalInt findBalance(Connection connection, long userId) throws SQLException {
        return findInt(connection, SQL_FIND_BALANCE, userId);
    }

    /**
     * Читает баланс кусочков пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @return баланс, если пользователь найден
     * @throws SQLException при ошибке выполнения запроса
     */
    OptionalInt findShards(Connection connection, long userId) throws SQLException {
        return findInt(connection, SQL_FIND_SHARDS, userId);
    }

    /**
     * Выполняет запрос, возвращающий новое значение баланса.
     *
     * @param connection соединение с БД
     * @param sql        SQL-запрос
     * @param amount     изменяемая сумма
     * @param userId     идентификатор пользователя
     * @param parameters дополнительные параметры запроса
     * @return новое значение баланса, если строка обновлена
     * @throws SQLException при ошибке выполнения запроса
     */
    private OptionalInt executeBalanceUpdate(Connection connection, String sql, int amount, long userId,
                                             int... parameters) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            int parameterIndex = 3;
            for (int parameter : parameters) {
                statement.setInt(parameterIndex++, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? OptionalInt.of(resultSet.getInt(1)) : OptionalInt.empty();
            }
        }
    }

    /**
     * Читает одно целочисленное значение баланса валюты или кусочков по идентификатору пользователя.
     *
     * @param connection соединение с БД
     * @param sql        SQL-запрос
     * @param userId     идентификатор пользователя
     * @return значение, если строка найдена
     * @throws SQLException при ошибке выполнения запроса
     */
    private OptionalInt findInt(Connection connection, String sql, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? OptionalInt.of(resultSet.getInt(1)) : OptionalInt.empty();
            }
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
                    0,
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
