package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.MarketListing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link InMemoryMarketRepository}: сохранение, выборка и удаление лотов.
 */
class InMemoryMarketRepositoryTest {

    /** Базовое время выставления лотов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Фейковый репозиторий. */
    private InMemoryMarketRepository repository;

    /** Фейковое соединение. */
    private FakeConnection connection;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMarketRepository();
        connection = new FakeConnection();
    }

    /**
     * Создаёт лот без идентификатора.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена лота
     * @param createdAt время выставления
     * @return лот
     */
    private MarketListing listing(long sellerId, long heroId, int price, LocalDateTime createdAt) {
        return new MarketListing(0, sellerId, heroId, price, createdAt);
    }

    @Test
    @DisplayName("saveListing присваивает идентификатор и сохраняет лот")
    void saveListingAssignsId() {
        MarketListing saved = repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));

        Assertions.assertTrue(saved.id() > 0);
        Assertions.assertEquals(1, repository.size());
        Assertions.assertEquals(saved, repository.findById(connection, saved.id()).orElseThrow());
    }

    @Test
    @DisplayName("saveListing сохраняет явно заданный идентификатор")
    void saveListingKeepsExplicitId() {
        MarketListing saved = repository.saveListing(connection,
                new MarketListing(42L, 1L, 7L, 100, CREATED_AT));

        Assertions.assertEquals(42L, saved.id());
        Assertions.assertTrue(repository.findById(connection, 42L).isPresent());
    }

    @Test
    @DisplayName("findById возвращает пустой результат для неизвестного лота")
    void findByIdReturnsEmptyForUnknownListing() {
        Assertions.assertTrue(repository.findById(connection, 999L).isEmpty());
    }

    @Test
    @DisplayName("findAllActive сортирует новые лоты первыми")
    void findAllActiveSortsNewestFirst() {
        MarketListing first = repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));
        MarketListing second = repository.saveListing(connection,
                listing(1L, 8L, 200, CREATED_AT.plusHours(1)));
        MarketListing third = repository.saveListing(connection,
                listing(1L, 9L, 300, CREATED_AT.plusHours(1)));

        List<MarketListing> all = repository.findAllActive();

        Assertions.assertEquals(List.of(third, second, first), all);
    }

    @Test
    @DisplayName("findBySeller возвращает только лоты продавца")
    void findBySellerFiltersBySeller() {
        repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));
        repository.saveListing(connection, listing(2L, 8L, 200, CREATED_AT));

        List<MarketListing> sellerListings = repository.findBySeller(1L);

        Assertions.assertEquals(1, sellerListings.size());
        Assertions.assertEquals(1L, sellerListings.getFirst().sellerId());
    }

    @Test
    @DisplayName("deleteListingAndReturn удаляет и возвращает лот")
    void deleteListingAndReturnRemovesListing() {
        MarketListing saved = repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));

        MarketListing deleted = repository.deleteListingAndReturn(connection, saved.id()).orElseThrow();

        Assertions.assertEquals(saved, deleted);
        Assertions.assertTrue(repository.findById(connection, saved.id()).isEmpty());
        Assertions.assertEquals(0, repository.size());
    }

    @Test
    @DisplayName("deleteListingAndReturn возвращает пустой результат для неизвестного лота")
    void deleteListingAndReturnReturnsEmptyForUnknownListing() {
        Assertions.assertTrue(repository.deleteListingAndReturn(connection, 999L).isEmpty());
    }

    @Test
    @DisplayName("deleteListingBySellerAndReturn не удаляет чужой лот")
    void deleteListingBySellerDoesNotRemoveAnotherSellerListing() {
        MarketListing saved = repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));

        Assertions.assertTrue(repository.deleteListingBySellerAndReturn(connection, saved.id(), 2L).isEmpty());
        Assertions.assertTrue(repository.findById(connection, saved.id()).isPresent());
    }

    @Test
    @DisplayName("deleteListingBySellerAndReturn удаляет лот владельца")
    void deleteListingBySellerRemovesOwnListing() {
        MarketListing saved = repository.saveListing(connection, listing(1L, 7L, 100, CREATED_AT));

        MarketListing deleted = repository
                .deleteListingBySellerAndReturn(connection, saved.id(), 1L)
                .orElseThrow();

        Assertions.assertEquals(saved, deleted);
        Assertions.assertTrue(repository.findById(connection, saved.id()).isEmpty());
    }
}
