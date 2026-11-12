package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.MarketRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Реализация {@link MarketService}.
 */
public class MarketServiceImpl implements MarketService {

    /** Имя продавца, если профиль не найден. */
    private static final String UNKNOWN_SELLER_NAME = "Avenger";

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /** Репозиторий карточек пользователей. */
    private final UserCardRepository userCardRepository;

    /** Репозиторий рынка. */
    private final MarketRepository marketRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /**
     * @param connectionFactory  фабрика соединений
     * @param userRepository     репозиторий пользователей
     * @param userCardRepository репозиторий карточек
     * @param marketRepository   репозиторий рынка
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

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found: " + heroId));
        validatePrice(price, hero.rarity());

        int available = userCardRepository.countByUserIdAndHeroId(sellerId, heroId);

        return JdbcTransactions.execute(connectionFactory, connection -> {
            if (!userCardRepository.removeCard(connection, sellerId, heroId, 1)) {
                throw new NotEnoughCardsException(heroId, 1, available);
            }
            MarketListing listing = new MarketListing(0, sellerId, heroId, price, LocalDateTime.now());
            return marketRepository.saveListing(connection, listing);
        });
    }

    @Override
    public @NotNull PurchaseResult buy(long buyerId, long listingId) throws AppException {
        MarketListing listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new ListingUnavailableException(listingId));
        if (listing.sellerId() == buyerId) {
            throw new OwnListingException(listingId);
        }
        int buyerBalance = userRepository.findByUserId(buyerId)
                .orElseThrow(() -> new UserNotFoundException(buyerId))
                .balance();
        int commission = listing.price() * COMMISSION_PERCENT / 100;
        int sellerAmount = listing.price() - commission;

        return JdbcTransactions.execute(connectionFactory, connection -> {
            MarketListing deleted = marketRepository.deleteListingAndReturn(connection, listingId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            if (!userRepository.spendBalance(connection, buyerId, deleted.price())) {
                throw new InsufficientFundsException(deleted.price(), buyerBalance);
            }
            userRepository.addBalance(connection, deleted.sellerId(), sellerAmount);
            userCardRepository.addCard(connection, buyerId, deleted.heroId());
            return new PurchaseResult(buyerId, deleted.sellerId(), deleted.heroId(), deleted.price());
        });
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        return JdbcTransactions.execute(connectionFactory, connection -> {
            MarketListing deleted = marketRepository
                    .deleteListingBySellerAndReturn(connection, listingId, sellerId)
                    .orElseThrow(() -> new ListingUnavailableException(listingId));
            userCardRepository.addCard(connection, sellerId, deleted.heroId());
            return deleted;
        });
    }

    @Override
    public @NotNull MarketItem getListing(long listingId) throws ListingUnavailableException {
        MarketListing listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new ListingUnavailableException(listingId));
        MarketItem item = toMarketItem(listing);
        if (item == null) {
            throw new ListingUnavailableException(listingId);
        }
        return item;
    }

    @Override
    public @NotNull List<MarketItem> getActiveListings() {
        return getActiveListings(null);
    }

    @Override
    public @NotNull List<MarketItem> getActiveListings(@Nullable HeroRarity rarity) {
        return marketRepository.findAllActive().stream()
                .filter(listing -> rarity == null || heroRepository.findById(listing.heroId())
                        .map(hero -> hero.rarity() == rarity)
                        .orElse(false))
                .map(this::toMarketItem)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public @NotNull List<MarketItem> getUserListings(long sellerId) {
        return marketRepository.findBySeller(sellerId).stream()
                .map(this::toMarketItem)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Проверяет допустимость цены для указанной редкости.
     *
     * @param price  цена лота
     * @param rarity редкость героя
     * @throws InvalidPriceException если цена вне допустимого диапазона
     */
    private void validatePrice(int price, HeroRarity rarity) throws InvalidPriceException {
        if (price < rarity.minPrice() || price > rarity.maxPrice()) {
            throw new InvalidPriceException(price, rarity.minPrice(), rarity.maxPrice());
        }
    }

    /**
     * Преобразует лот в объявление рынка.
     *
     * @param listing лот
     * @return объявление рынка или {@code null}, если герой отсутствует в справочнике
     */
    private @Nullable MarketItem toMarketItem(MarketListing listing) {
        Hero hero = heroRepository.findById(listing.heroId()).orElse(null);
        if (hero == null) {
            return null;
        }
        String sellerName = userRepository.findByUserId(listing.sellerId())
                .map(User::playerName)
                .map(PlayerName::value)
                .orElse(UNKNOWN_SELLER_NAME);
        return new MarketItem(listing, hero, sellerName);
    }
}
