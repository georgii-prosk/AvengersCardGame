package com.avengers.cardgame.fakes;

import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Фейковый сервис рынка для тестов обработчиков команд.
 * <p>
 * Позволяет задать возвращаемые значения и ошибки для каждого метода.
 */
public class FakeMarketService implements MarketService {

    /** Лот, возвращаемый при создании. */
    private MarketListing listingToCreate;

    /** Ошибка, выбрасываемая при создании лота. */
    private AppException createError;

    /** Результат покупки. */
    private PurchaseResult purchaseResult;

    /** Ошибка, выбрасываемая при покупке. */
    private AppException buyError;

    /** Лот, возвращаемый при снятии. */
    private MarketListing listingToCancel;

    /** Ошибка, выбрасываемая при снятии лота. */
    private AppException cancelError;

    /** Объявление, возвращаемое при поиске лота. */
    private MarketItem itemToGet;

    /** Активные лоты. */
    private List<MarketItem> activeListings = List.of();

    /** Лоты текущего пользователя. */
    private List<MarketItem> userListings = List.of();

    /**
     * Задаёт лот, возвращаемый при создании.
     *
     * @param listing лот
     */
    public void createReturns(@NotNull MarketListing listing) {
        this.listingToCreate = listing;
    }

    /**
     * Задаёт ошибку при создании лота.
     *
     * @param exception ошибка
     */
    public void createThrows(@NotNull AppException exception) {
        this.createError = exception;
    }

    /**
     * Задаёт результат покупки.
     *
     * @param result результат покупки
     */
    public void buyReturns(@NotNull PurchaseResult result) {
        this.purchaseResult = result;
    }

    /**
     * Задаёт ошибку при покупке.
     *
     * @param exception ошибка
     */
    public void buyThrows(@NotNull AppException exception) {
        this.buyError = exception;
    }

    /**
     * Задаёт лот, возвращаемый при снятии.
     *
     * @param listing лот
     */
    public void cancelReturns(@NotNull MarketListing listing) {
        this.listingToCancel = listing;
    }

    /**
     * Задаёт ошибку при снятии лота.
     *
     * @param exception ошибка
     */
    public void cancelThrows(@NotNull AppException exception) {
        this.cancelError = exception;
    }

    /**
     * Задаёт объявление, возвращаемое при поиске лота.
     *
     * @param item объявление
     */
    public void getListingReturns(@NotNull MarketItem item) {
        this.itemToGet = item;
    }

    /**
     * Задаёт активные лоты.
     *
     * @param items активные лоты
     */
    public void setActiveListings(@NotNull List<MarketItem> items) {
        this.activeListings = List.copyOf(items);
    }

    /**
     * Задаёт лоты текущего пользователя.
     *
     * @param items лоты пользователя
     */
    public void setUserListings(@NotNull List<MarketItem> items) {
        this.userListings = List.copyOf(items);
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException {
        if (createError != null) {
            throw createError;
        }
        if (listingToCreate == null) {
            throw new UnsupportedOperationException("FakeMarketService has no listing to create");
        }
        return listingToCreate;
    }

    @Override
    public @NotNull PurchaseResult buy(long buyerId, long listingId) throws AppException {
        if (buyError != null) {
            throw buyError;
        }
        if (purchaseResult == null) {
            throw new UnsupportedOperationException("FakeMarketService has no purchase result");
        }
        return purchaseResult;
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException {
        if (cancelError != null) {
            throw cancelError;
        }
        if (listingToCancel == null) {
            throw new UnsupportedOperationException("FakeMarketService has no listing to cancel");
        }
        return listingToCancel;
    }

    @Override
    public @NotNull MarketItem getListing(long listingId) throws ListingUnavailableException {
        if (itemToGet == null) {
            throw new ListingUnavailableException(listingId);
        }
        return itemToGet;
    }

    @Override
    public @NotNull List<MarketItem> getActiveListings() {
        return getActiveListings(null);
    }

    @Override
    public @NotNull List<MarketItem> getActiveListings(@Nullable HeroRarity rarity) {
        return activeListings.stream()
                .filter(item -> rarity == null || item.hero().rarity() == rarity)
                .toList();
    }

    @Override
    public @NotNull List<MarketItem> getUserListings(long sellerId) {
        return userListings;
    }
}
