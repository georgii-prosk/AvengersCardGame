package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.MarketRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Реализация {@link MarketService}.
 */
public class MarketServiceImpl implements MarketService {

    /** Имя продавца, если профиль не найден. */
    private static final String UNKNOWN_SELLER_NAME = "Avenger";

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /** Репозиторий рынка. */
    private final MarketRepository marketRepository;

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /**
     * @param userRepository   репозиторий пользователей
     * @param marketRepository репозиторий рынка
     * @param heroRepository   репозиторий героев
     * @param gameConfig       игровая конфигурация
     */
    public MarketServiceImpl(UserRepository userRepository, MarketRepository marketRepository,
                             HeroRepository heroRepository, GameConfig gameConfig) {
        this.userRepository = userRepository;
        this.marketRepository = marketRepository;
        this.heroRepository = heroRepository;
        this.gameConfig = gameConfig;
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        Hero hero = heroRepository.findById(heroId)
                .orElseThrow(() -> new IllegalArgumentException("Hero not found: " + heroId));
        validatePrice(price, hero.rarity());

        return marketRepository.createListing(sellerId, heroId, price);
    }

    @Override
    public @NotNull PurchaseResult buy(long buyerId, long listingId) throws AppException {
        MarketListing listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new ListingUnavailableException(listingId));
        if (listing.sellerId() == buyerId) {
            throw new OwnListingException(listingId);
        }
        int commission = listing.price() * gameConfig.marketCommissionPercent() / 100;
        int sellerAmount = listing.price() - commission;

        MarketListing deleted = marketRepository.buy(buyerId, listingId, sellerAmount);
        return new PurchaseResult(buyerId, deleted.sellerId(), deleted.heroId(), deleted.price());
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        return marketRepository.cancelListing(sellerId, listingId);
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
        GameConfig.RarityConfig rarityConfig = gameConfig.rarity(rarity);
        if (price < rarityConfig.minPrice() || price > rarityConfig.maxPrice()) {
            throw new InvalidPriceException(price, rarityConfig.minPrice(), rarityConfig.maxPrice());
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
