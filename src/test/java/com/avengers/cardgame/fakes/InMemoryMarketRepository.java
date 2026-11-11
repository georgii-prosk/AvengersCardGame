package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.repository.MarketRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Репозиторий рынка в памяти.
 */
public class InMemoryMarketRepository implements MarketRepository {

    /** Хранилище лотов по идентификатору. */
    private final Map<Long, MarketListing> storage = new HashMap<>();

    /** Следующий идентификатор лота. */
    private long nextId = 1;

    /**
     * Сохраняет новый лот.
     *
     * @param listing лот для сохранения
     * @return сохранённый лот с присвоенным идентификатором
     */
    public @NotNull MarketListing saveListing(@NotNull MarketListing listing) {
        return saveListing(null, listing);
    }

    @Override
    public @NotNull MarketListing saveListing(@Nullable Connection connection, @NotNull MarketListing listing) {
        long id = listing.id() == 0 ? nextId++ : listing.id();
        MarketListing saved = new MarketListing(id, listing.sellerId(), listing.heroId(),
                listing.price(), listing.createdAt());
        storage.put(id, saved);
        return saved;
    }

    /**
     * Находит лот по идентификатору.
     *
     * @param listingId идентификатор лота
     * @return лот, если найден
     */
    @Override
    public @NotNull Optional<MarketListing> findById(long listingId) {
        return Optional.ofNullable(storage.get(listingId));
    }

    @Override
    public @NotNull List<MarketListing> findAllActive() {
        return sorted(storage.values());
    }

    @Override
    public @NotNull List<MarketListing> findBySeller(long sellerId) {
        return sorted(storage.values().stream()
                .filter(listing -> listing.sellerId() == sellerId)
                .toList());
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingAndReturn(
            @Nullable Connection connection, long listingId) {
        return Optional.ofNullable(storage.remove(listingId));
    }

    @Override
    public @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @Nullable Connection connection, long listingId, long sellerId) {
        MarketListing listing = storage.get(listingId);
        if (listing == null || listing.sellerId() != sellerId) {
            return Optional.empty();
        }
        storage.remove(listingId);
        return Optional.of(listing);
    }

    /**
     * Сортирует лоты по идентификатору.
     *
     * @param listings лоты
     * @return отсортированный список
     */
    private List<MarketListing> sorted(java.util.Collection<MarketListing> listings) {
        List<MarketListing> result = new ArrayList<>(listings);
        result.sort(Comparator.comparingLong(MarketListing::id));
        return result;
    }

    /**
     * @return количество сохранённых лотов
     */
    public int size() {
        return storage.size();
    }
}
