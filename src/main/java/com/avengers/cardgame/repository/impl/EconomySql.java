package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.model.MarketListing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * SQL-операции экономики, общие для JDBC-репозиториев.
 * <p>
 * Класс пакетно-приватный: он является деталью реализации и не входит в контракты.
 */
final class EconomySql {

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

    /** Запрос для подсчёта количества копий конкретной карточки. */
    private static final String SQL_COUNT_CARD =
            "SELECT COALESCE(SUM(count), 0) FROM user_cards WHERE user_id = ? AND hero_id = ?";

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

    /** Запрос для сохранения лота с возвратом данных. */
    private static final String SQL_INSERT_LISTING = """
            INSERT INTO market_listings (seller_id, hero_id, price)
            VALUES (?, ?, ?)
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /** Запрос для удаления лота с возвратом данных. */
    private static final String SQL_DELETE_LISTING = """
            DELETE FROM market_listings WHERE id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /** Запрос для удаления лота продавца с возвратом данных. */
    private static final String SQL_DELETE_LISTING_BY_SELLER = """
            DELETE FROM market_listings WHERE id = ? AND seller_id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /**
     * Добавляет карточку пользователю (или увеличивает количество).
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @throws SQLException при ошибке выполнения запроса
     */
    void addCard(Connection connection, long userId, long heroId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            statement.executeUpdate();
        }
    }

    /**
     * Удаляет указанное количество копий карточки.
     * <p>
     * При достижении нулевого количества строка удаляется.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @param count      количество копий для удаления
     * @return {@code true}, если удаление выполнено; {@code false}, если копий недостаточно
     * @throws SQLException при ошибке выполнения запроса
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean removeCard(Connection connection, long userId, long heroId, int count) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_REMOVE_CARD)) {
            statement.setInt(1, count);
            statement.setLong(2, userId);
            statement.setLong(3, heroId);
            statement.setInt(4, count);
            if (statement.executeUpdate() == 0) {
                return false;
            }
        }
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_EMPTY_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            statement.executeUpdate();
        }
        return true;
    }

    /**
     * Считает количество копий карточки у пользователя.
     *
     * @param connection соединение с БД
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @return количество копий карточки
     * @throws SQLException при ошибке выполнения запроса
     */
    int countCards(Connection connection, long userId, long heroId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_COUNT_CARD)) {
            statement.setLong(1, userId);
            statement.setLong(2, heroId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
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
        try (PreparedStatement statement = connection.prepareStatement(SQL_SPEND_BALANCE)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            statement.setInt(3, amount);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? OptionalInt.of(resultSet.getInt(1)) : OptionalInt.empty();
            }
        }
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
        try (PreparedStatement statement = connection.prepareStatement(SQL_SPEND_SHARDS)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            statement.setInt(3, amount);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? OptionalInt.of(resultSet.getInt(1)) : OptionalInt.empty();
            }
        }
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
        try (PreparedStatement statement = connection.prepareStatement(SQL_ADD_SHARDS)) {
            statement.setInt(1, amount);
            statement.setLong(2, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? OptionalInt.of(resultSet.getInt(1)) : OptionalInt.empty();
            }
        }
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
     * Сохраняет новый лот.
     *
     * @param connection соединение с БД
     * @param sellerId   идентификатор продавца
     * @param heroId     идентификатор героя
     * @param price      цена лота
     * @return сохранённый лот с присвоенным идентификатором
     * @throws SQLException при ошибке выполнения запроса
     */
    MarketListing insertListing(Connection connection, long sellerId, long heroId, int price)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_LISTING)) {
            statement.setLong(1, sellerId);
            statement.setLong(2, heroId);
            statement.setInt(3, price);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapListing(resultSet);
                }
            }
        }
        throw new IllegalStateException("Failed to save market listing");
    }

    /**
     * Удаляет лот и возвращает его данные.
     *
     * @param connection соединение с БД
     * @param listingId  идентификатор лота
     * @return удалённый лот, если он существовал
     * @throws SQLException при ошибке выполнения запроса
     */
    Optional<MarketListing> deleteListing(Connection connection, long listingId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_LISTING)) {
            statement.setLong(1, listingId);
            return readListing(statement);
        }
    }

    /**
     * Удаляет лот продавца и возвращает его данные.
     *
     * @param connection соединение с БД
     * @param listingId  идентификатор лота
     * @param sellerId   идентификатор продавца
     * @return удалённый лот, если он существовал и принадлежал продавцу
     * @throws SQLException при ошибке выполнения запроса
     */
    Optional<MarketListing> deleteListingBySeller(Connection connection, long listingId, long sellerId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_LISTING_BY_SELLER)) {
            statement.setLong(1, listingId);
            statement.setLong(2, sellerId);
            return readListing(statement);
        }
    }

    /**
     * Выполняет SELECT одного целого значения.
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
     * Читает удалённый лот из результата запроса.
     *
     * @param statement выполненный запрос
     * @return лот, если запрос вернул строку
     * @throws SQLException при ошибке чтения
     */
    private Optional<MarketListing> readListing(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? Optional.of(mapListing(resultSet)) : Optional.empty();
        }
    }

    /**
     * Преобразует текущую строку результата в лот.
     *
     * @param resultSet результат SQL-запроса
     * @return заполненный лот
     * @throws SQLException при ошибке чтения полей
     */
    private MarketListing mapListing(ResultSet resultSet) throws SQLException {
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        return new MarketListing(
                resultSet.getLong("id"),
                resultSet.getLong("seller_id"),
                resultSet.getLong("hero_id"),
                resultSet.getInt("price"),
                createdAt.toLocalDateTime()
        );
    }
}
