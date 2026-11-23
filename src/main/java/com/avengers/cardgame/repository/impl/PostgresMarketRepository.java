package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
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
import java.util.OptionalInt;

/**
 * Реализация {@link MarketRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresMarketRepository extends JdbcSupport implements MarketRepository {

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

    /** SQL-операции экономики. */
    private final EconomySql economySql = new EconomySql();

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresMarketRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        return inTransaction(connection -> {
            if (!economySql.removeCard(connection, sellerId, heroId, 1)) {
                throw new NotEnoughCardsException(heroId, 1,
                        economySql.countCards(connection, sellerId, heroId));
            }
            return economySql.insertListing(connection, sellerId, heroId, price);
        });
    }

    @Override
    public @NotNull MarketListing buy(long buyerId, long listingId, int sellerAmount) throws AppException {
        return inTransaction(connection -> {
            MarketListing deleted = economySql.deleteListing(connection, listingId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            OptionalInt balance = economySql.spendBalance(connection, buyerId, deleted.price());
            if (balance.isEmpty()) {
                OptionalInt available = economySql.findBalance(connection, buyerId);
                if (available.isEmpty()) {
                    throw new UserNotFoundException(buyerId);
                }
                throw new InsufficientFundsException(deleted.price(), available.getAsInt());
            }
            if (!economySql.addBalance(connection, deleted.sellerId(), sellerAmount)) {
                throw new UserNotFoundException(deleted.sellerId());
            }
            economySql.addCard(connection, buyerId, deleted.heroId());
            return deleted;
        });
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        return inTransaction(connection -> {
            MarketListing deleted = economySql.deleteListingBySeller(connection, listingId, sellerId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            economySql.addCard(connection, sellerId, deleted.heroId());
            return deleted;
        });
    }

    /**
     * Находит лот по идентификатору.
     *
     * @param listingId идентификатор лота
     * @return лот, если найден
     */
    @Override
    public @NotNull Optional<MarketListing> findById(long listingId) {
        try (Connection connection = connectionFactory.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_FIND_BY_ID)) {
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
        return findMany(SQL_FIND_ALL, null);
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        return findMany(SQL_FIND_BY_SELLER, sellerId);
    }

    /**
     * Выполняет запрос, возвращающий список лотов.
     *
     * @param sql      SQL-запрос
     * @param sellerId идентификатор продавца, либо {@code null}, если параметр не нужен
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
