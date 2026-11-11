package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PurchaseResult;
import com.avengers.cardgame.service.MarketService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Тестовый сервис рынка, возвращающий заранее заданные лоты.
 */
public class FakeMarketService implements MarketService {

    /** Активные лоты. */
    private final List<MarketItem> listings;

    /**
     * @param listings активные лоты
     */
    public FakeMarketService(List<MarketItem> listings) {
        this.listings = List.copyOf(listings);
    }

    /**
     * Создаёт пустой сервис рынка.
     */
    public FakeMarketService() {
        this(List.of());
    }

    @Override
    public @NotNull MarketListing createListing(long sellerId, long heroId, int price) {
        throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public @NotNull PurchaseResult buy(long buyerId, long listingId) {
        throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public @NotNull MarketListing cancelListing(long sellerId, long listingId) {
        throw new UnsupportedOperationException("Not used in this test");
    }

    @Override
    public @NotNull List<MarketItem> getActiveListings() {
        return listings;
    }

    @Override
    public @NotNull List<MarketItem> getUserListings(long sellerId) {
        return listings.stream().filter(item -> item.listing().sellerId() == sellerId).toList();
    }
}
