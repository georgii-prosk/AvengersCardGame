package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.repository.MarketRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация {@link MarketRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresMarketRepository extends JdbcSupport implements MarketRepository {

    /** Запрос для сохранения лота. */
    private static final String SQL_INSERT = """
            INSERT INTO market_listings (seller_id, hero_id, price)
            VALUES (?, ?, ?)
            RETURNING id, seller_id, hero_id, price, created_at
            """;

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

    /** Запрос для удаления лота с возвратом данных. */
    private static final String SQL_DELETE_AND_RETURN = """
            DELETE FROM market_listings WHERE id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /** Запрос для удаления лота продавца с возвратом данных. */
    private static final String SQL_DELETE_BY_SELLER_AND_RETURN = """
            DELETE FROM market_listings WHERE id = ? AND seller_id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresMarketRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public @NotNull MarketListing saveListing(@Nullable Connection connection, @NotNull MarketListing listing) {
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_INSERT)) {
                statement.setLong(1, listing.sellerId());
                statement.setLong(2, listing.heroId());
                statement.setInt(3, listing.price());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return mapRowToListing(resultSet);
                    }
                }
            }
            throw new IllegalStateException("Failed to save market listing");
        } catch (SQLException exception) {
            throw new RuntimeException("Error saving market listing", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
    }

    /**
     * Находит лот по идентификатору.
     *
     * @param listingId идентификатор лота
     * @return лот, если найден
     */
    @Override
    public @NotNull Optional<MarketListing> findById(long listingId) {
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(null);
            try (PreparedStatement statement = actualConnection.prepareStatement(SQL_FIND_BY_ID)) {
                statement.setLong(1, listingId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToListing(resultSet));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listing", exception);
        } finally {
            closeIfOwned(null, actualConnection);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        return findMany(SQL_FIND_ALL, null);
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        return findMany(SQL_FIND_BY_SELLER, sellerId);
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingAndReturn(
            @Nullable Connection connection, long listingId) {
        return deleteAndReturn(connection, SQL_DELETE_AND_RETURN, listingId, null);
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @Nullable Connection connection, long listingId, long sellerId) {
        return deleteAndReturn(connection, SQL_DELETE_BY_SELLER_AND_RETURN, listingId, sellerId);
    }

    /**
     * Выполняет запрос, возвращающий список лотов.
     *
     * @param sql           SQL-запрос
     * @param sellerId      идентификатор продавца, либо {@code null}, если параметр не нужен
     * @return список лотов
     */
    private List<MarketListing> findMany(String sql, @Nullable Long sellerId) {
        List<MarketListing> listings = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (sellerId != null) {
                statement.setLong(1, sellerId);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    listings.add(mapRowToListing(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listings", exception);
        }
        return listings;
    }

    /**
     * Удаляет лот и возвращает его данные.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param sql        SQL-запрос удаления
     * @param listingId  идентификатор лота
     * @param sellerId   идентификатор продавца, либо {@code null}, если проверка не нужна
     * @return удалённый лот, если он существовал
     */
    private Optional<MarketListing> deleteAndReturn(
            @Nullable Connection connection, String sql, long listingId, @Nullable Long sellerId) {
        Connection actualConnection = null;
        try {
            actualConnection = acquireConnection(connection);
            try (PreparedStatement statement = actualConnection.prepareStatement(sql)) {
                statement.setLong(1, listingId);
                if (sellerId != null) {
                    statement.setLong(2, sellerId);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return Optional.of(mapRowToListing(resultSet));
                    }
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error deleting market listing", exception);
        } finally {
            closeIfOwned(connection, actualConnection);
        }
        return Optional.empty();
    }

    /**
     * Преобразует текущую строку {@link ResultSet} в объект {@link MarketListing}.
     *
     * @param resultSet результат SQL-запроса
     * @return заполненный лот
     * @throws SQLException при ошибке чтения полей
     */
    private MarketListing mapRowToListing(ResultSet resultSet) throws SQLException {
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
