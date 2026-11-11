package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.repository.MarketRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Репозиторий лотов рынка в памяти.
 */
public class InMemoryMarketRepository implements MarketRepository {

    /** Хранилище лотов по идентификатору. */
    private final Map<Long, MarketListing> storage = new LinkedHashMap<>();

    /** Счётчик идентификаторов. */
    private final AtomicLong nextId = new AtomicLong(1);

    @Override
    public @NotNull MarketListing saveListing(@NotNull Connection connection, @NotNull MarketListing listing) {
        long id = listing.id() > 0 ? listing.id() : nextId.getAndIncrement();
        MarketListing saved = new MarketListing(id, listing.sellerId(), listing.heroId(),
                listing.price(), listing.createdAt());
        storage.put(id, saved);
        return saved;
    }

    @Override
    public @NotNull Optional<MarketListing> findById(@NotNull Connection connection, long listingId) {
        return Optional.ofNullable(storage.get(listingId));
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        return storage.values().stream()
                .sorted(Comparator.comparing(MarketListing::createdAt).reversed()
                        .thenComparing(Comparator.comparingLong(MarketListing::id).reversed()))
                .toList();
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        return findAllActive().stream()
                .filter(listing -> listing.sellerId() == sellerId)
                .toList();
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingAndReturn(
            @NotNull Connection connection, long listingId) {
        return Optional.ofNullable(storage.remove(listingId));
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @NotNull Connection connection, long listingId, long sellerId) {
        MarketListing listing = storage.get(listingId);
        if (listing == null || listing.sellerId() != sellerId) {
            return Optional.empty();
        }
        storage.remove(listingId);
        return Optional.of(listing);
    }

    /**
     * Возвращает количество сохранённых лотов.
     *
     * @return количество лотов
     */
    public int size() {
        return storage.size();
    }
}
