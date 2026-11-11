package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.repository.MarketRepository;
import org.jetbrains.annotations.NotNull;

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
 * Реализация {@link MarketRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresMarketRepository implements MarketRepository {

    /** Запрос для вставки лота с возвратом идентификатора. */
    private static final String SQL_INSERT = """
            INSERT INTO market_listings (seller_id, hero_id, price, created_at)
            VALUES (?, ?, ?, ?)
            RETURNING id
            """;

    /** Запрос для поиска лота по идентификатору. */
    private static final String SQL_FIND_BY_ID =
            "SELECT id, seller_id, hero_id, price, created_at FROM market_listings WHERE id = ?";

    /** Запрос для поиска всех активных лотов. */
    private static final String SQL_FIND_ALL =
            "SELECT id, seller_id, hero_id, price, created_at FROM market_listings ORDER BY created_at DESC, id DESC";

    /** Запрос для поиска лотов продавца. */
    private static final String SQL_FIND_BY_SELLER = """
            SELECT id, seller_id, hero_id, price, created_at
            FROM market_listings WHERE seller_id = ?
            ORDER BY created_at DESC, id DESC
            """;

    /** Запрос для удаления лота с возвратом данных. */
    private static final String SQL_DELETE_RETURNING = """
            DELETE FROM market_listings WHERE id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /** Запрос для удаления лота продавца с возвратом данных. */
    private static final String SQL_DELETE_BY_SELLER_RETURNING = """
            DELETE FROM market_listings WHERE id = ? AND seller_id = ?
            RETURNING id, seller_id, hero_id, price, created_at
            """;

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresMarketRepository(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public @NotNull MarketListing saveListing(@NotNull Connection connection, @NotNull MarketListing listing) {
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setLong(1, listing.sellerId());
            statement.setLong(2, listing.heroId());
            statement.setInt(3, listing.price());
            statement.setTimestamp(4, Timestamp.valueOf(listing.createdAt()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new MarketListing(resultSet.getLong("id"), listing.sellerId(), listing.heroId(),
                            listing.price(), listing.createdAt());
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error saving market listing", exception);
        }
        throw new RuntimeException("Saving market listing did not return an identifier");
    }

    @Override
    public @NotNull Optional<MarketListing> findById(@NotNull Connection connection, long listingId) {
        try (PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {
            statement.setLong(1, listingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToListing(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listing", exception);
        }
        return Optional.empty();
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        return findList(SQL_FIND_ALL, null);
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        return findList(SQL_FIND_BY_SELLER, sellerId);
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingAndReturn(
            @NotNull Connection connection, long listingId) {
        return deleteReturning(connection, listingId);
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @NotNull Connection connection, long listingId, long sellerId) {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_SELLER_RETURNING)) {
            statement.setLong(1, listingId);
            statement.setLong(2, sellerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToListing(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error deleting market listing", exception);
        }
        return Optional.empty();
    }

    /**
     * Выполняет удаление лота с возвратом удалённой строки.
     *
     * @param connection соединение с активной транзакцией
     * @param listingId  идентификатор лота
     * @return удалённый лот, если он существовал
     */
    private Optional<MarketListing> deleteReturning(
            @NotNull Connection connection, long listingId) {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DELETE_RETURNING)) {
            statement.setLong(1, listingId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapRowToListing(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Error deleting market listing", exception);
        }
        return Optional.empty();
    }

    /**
     * Выполняет запрос списка лотов.
     *
     * @param query       query-запрос
     * @param sellerId  идентификатор продавца или {@code null}, если фильтр не нужен
     * @return список лотов
     */
    private List<MarketListing> findList(String query, Long sellerId) {
        List<MarketListing> listings = new ArrayList<>();
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
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
     * Преобразует строку результата в лот.
     *
     * @param resultSet результат query-запроса
     * @return лот
     * @throws SQLException при ошибке чтения полей
     */
    private MarketListing mapRowToListing(ResultSet resultSet) throws SQLException {
        LocalDateTime createdAt = resultSet.getTimestamp("created_at").toLocalDateTime();
        return new MarketListing(
                resultSet.getLong("id"),
                resultSet.getLong("seller_id"),
                resultSet.getLong("hero_id"),
                resultSet.getInt("price"),
                createdAt
        );
    }
}
