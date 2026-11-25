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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Реализация {@link MarketRepository} на основе JDBC (PostgreSQL).
 */
public class PostgresMarketRepository extends JdbcSupport implements MarketRepository {

    /** DAO лотов рынка. */
    private final MarketListingDao marketListingDao = new MarketListingDao();

    /** DAO карточек пользователей. */
    private final UserCardDao userCardDao = new UserCardDao();

    /** DAO пользователей. */
    private final UserDao userDao = new UserDao();

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    public PostgresMarketRepository(ConnectionFactory connectionFactory) {
        super(connectionFactory);
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        return inTransaction(connection -> {
            if (userCardDao.removeCard(connection, sellerId, heroId, 1) == 0) {
                throw new NotEnoughCardsException(heroId, 1,
                        userCardDao.countCards(connection, sellerId, heroId));
            }
            return marketListingDao.insertListing(connection, sellerId, heroId, price);
        });
    }

    @Override
    public @NotNull MarketListing buy(long buyerId, long listingId, int sellerAmount) throws AppException {
        return inTransaction(connection -> {
            MarketListing deleted = marketListingDao.deleteListing(connection, listingId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            OptionalInt balance = userDao.spendBalance(connection, buyerId, deleted.price());
            if (balance.isEmpty()) {
                OptionalInt available = userDao.findBalance(connection, buyerId);
                if (available.isEmpty()) {
                    throw new UserNotFoundException(buyerId);
                }
                throw new InsufficientFundsException(deleted.price(), available.getAsInt());
            }
            if (!userDao.addBalance(connection, deleted.sellerId(), sellerAmount)) {
                throw new UserNotFoundException(deleted.sellerId());
            }
            userCardDao.addCard(connection, buyerId, deleted.heroId());
            return deleted;
        });
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        return inTransaction(connection -> {
            MarketListing deleted = marketListingDao.deleteListingBySeller(connection, listingId, sellerId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            userCardDao.addCard(connection, sellerId, deleted.heroId());
            return deleted;
        });
    }

    @Override
    public @NotNull Optional<MarketListing> findById(long listingId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return marketListingDao.findById(connection, listingId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listing", exception);
        }
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        try (Connection connection = connectionFactory.getConnection()) {
            return marketListingDao.findAllActive(connection);
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listings", exception);
        }
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        try (Connection connection = connectionFactory.getConnection()) {
            return marketListingDao.findBySeller(connection, sellerId);
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding market listings", exception);
        }
    }
}
