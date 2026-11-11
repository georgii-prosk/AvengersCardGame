package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PurchaseResult;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.MarketRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация {@link MarketService} с локальными JDBC-транзакциями.
 */
public class MarketServiceImpl implements MarketService {

    /** Комиссия рынка в процентах. */
    public static final int COMMISSION_PERCENT = 5;

    /** Имя продавца, если пользователь не найден. */
    private static final String UNKNOWN_SELLER_NAME = "Неизвестный игрок";

    /** Логгер. */
    private static final Logger logger = LoggerFactory.getLogger(MarketServiceImpl.class);

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий лотов рынка. */
    private final MarketRepository marketRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /**
     * @param connectionFactory  фабрика соединений
     * @param userRepository     репозиторий пользователей
     * @param userCardRepository репозиторий карточек
     * @param marketRepository   репозиторий лотов
     * @param heroRepository     репозиторий героев
     */
    public MarketServiceImpl(ConnectionFactory connectionFactory, UserRepository userRepository,
                             UserCardRepository userCardRepository, MarketRepository marketRepository,
                             HeroRepository heroRepository) {
        this.connectionFactory = connectionFactory;
        this.userRepository = userRepository;
        this.userCardRepository = userCardRepository;
        this.marketRepository = marketRepository;
        this.heroRepository = heroRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price)
            throws UserNotFoundException, InvalidPriceException, NotEnoughCardsException {
        User seller = userRepository.findByUserId(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown hero: " + heroId));
        validatePrice(hero.rarity(), price);
        int available = availableCards(seller.userId(), heroId);

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);

            boolean cardRemoved = userCardRepository.removeCard(connection, sellerId, heroId);
            if (!cardRemoved) {
                throw new NotEnoughCardsException(sellerId, heroId, 1, available);
            }

            MarketListing listing = marketRepository.saveListing(connection,
                    new MarketListing(0, sellerId, heroId, price, LocalDateTime.now()));
            connection.commit();
            return listing;
        } catch (AppException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Error creating market listing", exception);
        } finally {
            close(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull PurchaseResult buy(long buyerId, long listingId)
            throws UserNotFoundException, InsufficientFundsException, ListingUnavailableException {
        User buyer = userRepository.findByUserId(buyerId)
                .orElseThrow(() -> new UserNotFoundException(buyerId));

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);

            MarketListing listing = marketRepository.deleteListingAndReturn(connection, listingId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            if (listing.sellerId() == buyerId) {
                throw new ListingUnavailableException(listingId, "cannot buy own listing");
            }

            if (!userRepository.spendBalance(connection, buyerId, listing.price())) {
                throw new InsufficientFundsException(listing.price(), buyer.balance());
            }

            int sellerAmount = listing.price() - commission(listing.price());
            if (!userRepository.addBalance(connection, listing.sellerId(), sellerAmount)) {
                throw new UserNotFoundException(listing.sellerId());
            }

            userCardRepository.addCard(connection, buyerId, listing.heroId());
            connection.commit();
            return new PurchaseResult(buyerId, listing.sellerId(), listing.heroId(), listing.price());
        } catch (AppException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Error buying market listing", exception);
        } finally {
            close(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId)
            throws UserNotFoundException, ListingUnavailableException {
        userRepository.findByUserId(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));

        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);

            MarketListing listing = marketRepository
                    .deleteListingBySellerAndReturn(connection, listingId, sellerId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId, "not owned by seller"));
            userCardRepository.addCard(connection, sellerId, listing.heroId());

            connection.commit();
            return listing;
        } catch (AppException | RuntimeException exception) {
            rollback(connection);
            throw exception;
        } catch (SQLException exception) {
            rollback(connection);
            throw new RuntimeException("Error cancelling market listing", exception);
        } finally {
            close(connection);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<MarketItem> getActiveListings() {
        return toMarketItems(marketRepository.findAllActive());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull List<MarketItem> getUserListings(long sellerId) {
        return toMarketItems(marketRepository.findBySeller(sellerId));
    }

    /**
     * Обогащает лоты данными героев и продавцов.
     *
     * @param listings список лотов
     * @return список элементов рынка
     */
    private List<MarketItem> toMarketItems(List<MarketListing> listings) {
        List<MarketItem> items = new ArrayList<>(listings.size());
        for (MarketListing listing : listings) {
            Hero hero = heroRepository.findById(listing.heroId()).orElse(null);
            if (hero == null) {
                logger.warn("Skipping market listing {}: hero {} not found", listing.id(), listing.heroId());
                continue;
            }
            String sellerName = userRepository.findByUserId(listing.sellerId())
                    .map(user -> user.playerName().value())
                    .orElse(UNKNOWN_SELLER_NAME);
            items.add(new MarketItem(listing, hero, sellerName));
        }
        return items;
    }

    /**
     * Проверяет цену лота по рамкам редкости.
     *
     * @param rarity редкость героя
     * @param price  цена лота
     * @throws InvalidPriceException если цена вне допустимого диапазона
     */
    private void validatePrice(HeroRarity rarity, int price) throws InvalidPriceException {
        if (price < rarity.minPrice() || price > rarity.maxPrice()) {
            throw new InvalidPriceException(price, rarity.minPrice(), rarity.maxPrice());
        }
    }

    /**
     * Считает количество копий карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    private int availableCards(long userId, long heroId) {
        return userCardRepository.findByUserId(userId).stream()
                .filter(card -> card.heroId() == heroId)
                .mapToInt(UserCard::count)
                .sum();
    }

    /**
     * Вычисляет комиссию рынка.
     *
     * @param price цена сделки
     * @return размер комиссии
     */
    private int commission(int price) {
        return price * COMMISSION_PERCENT / 100;
    }

    /**
     * Откатывает транзакцию, логируя ошибку отката.
     *
     * @param connection соединение или {@code null}
     */
    private void rollback(Connection connection) {
        if (connection == null) return;
        try {
            connection.rollback();
        } catch (SQLException exception) {
            logger.error("Failed to rollback transaction", exception);
        }
    }

    /**
     * Закрывает соединение, логируя ошибку закрытия.
     *
     * @param connection соединение или {@code null}
     */
    private void close(Connection connection) {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException exception) {
            logger.error("Failed to close connection", exception);
        }
    }
}
