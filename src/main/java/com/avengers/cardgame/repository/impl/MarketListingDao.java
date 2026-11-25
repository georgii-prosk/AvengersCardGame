package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.model.MarketListing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO для работы с таблицей {@code market_listings}.
 */
final class MarketListingDao {

    /** Запрос для поиска лота по идентификатору. */
    private static final String SQL_FIND_BY_ID = """
            SELECT id, seller_id, hero_id, price, created_at
            FROM market_listings WHERE id = ?
            """;

    /** Запрос для поиска всех активных лотов. */
    private static final String SQL_FIND_ALL = """
            SELECT id, seller_id, hero_id, price, created_at
            FROM market_listings ORDER BY id
            """;

    /** Запрос для поиска лотов продавца. */
    private static final String SQL_FIND_BY_SELLER = """
            SELECT id, seller_id, hero_id, price, created_at
            FROM market_listings WHERE seller_id = ? ORDER BY id
            """;

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
     * Находит лот по идентификатору.
     *
     * @param connection соединение с БД
     * @param listingId  идентификатор лота
     * @return лот, если найден
     * @throws SQLException при ошибке выполнения запроса
     */
    Optional<MarketListing> findById(Connection connection, long listingId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {
            statement.setLong(1, listingId);
            return readListing(statement);
        }
    }

    /**
     * Возвращает все активные лоты.
     *
     * @param connection соединение с БД
     * @return список лотов
     * @throws SQLException при ошибке выполнения запроса
     */
    List<MarketListing> findAllActive(Connection connection) throws SQLException {
        List<MarketListing> listings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_ALL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                listings.add(mapListing(resultSet));
            }
        }
        return listings;
    }

    /**
     * Возвращает все активные лоты продавца.
     *
     * @param connection соединение с БД
     * @param sellerId   идентификатор продавца
     * @return список лотов
     * @throws SQLException при ошибке выполнения запроса
     */
    List<MarketListing> findBySeller(Connection connection, long sellerId) throws SQLException {
        List<MarketListing> listings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_SELLER)) {
            statement.setLong(1, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    listings.add(mapListing(resultSet));
                }
            }
        }
        return listings;
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
    MarketListing insertListing(Connection connection, long sellerId, long heroId, int price) throws SQLException {
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
     * Читает удалённый или найденный лот из результата запроса.
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
