package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.MarketListing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link InMemoryMarketRepository}.
 */
class InMemoryMarketRepositoryTest {

    /** Время создания лота для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Репозиторий рынка. */
    private InMemoryMarketRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryMarketRepository();
    }

    /**
     * Создаёт лот без идентификатора.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена
     * @return новый лот
     */
    private MarketListing listing(long sellerId, long heroId, int price) {
        return new MarketListing(0, sellerId, heroId, price, CREATED_AT);
    }

    /**
     * Проверяет, что сохранение назначает идентификатор.
     */
    @Test
    @DisplayName("Назначает идентификатор при сохранении")
    void assignsIdOnSave() {
        MarketListing first = repository.saveListing(listing(1L, 10L, 100));
        MarketListing second = repository.saveListing(listing(1L, 11L, 100));

        Assertions.assertEquals(1L, first.id());
        Assertions.assertEquals(2L, second.id());
        Assertions.assertEquals(2, repository.size());
    }

    /**
     * Проверяет, что сохранение с готовым идентификатором его сохраняет.
     */
    @Test
    @DisplayName("Сохраняет явный идентификатор")
    void keepsExplicitId() {
        MarketListing saved = repository.saveListing(
                new MarketListing(50L, 1L, 10L, 100, CREATED_AT));

        Assertions.assertEquals(50L, saved.id());
        Assertions.assertTrue(repository.findById(50L).isPresent());
    }

    /**
     * Проверяет поиск по идентификатору.
     */
    @Test
    @DisplayName("Находит лот по идентификатору")
    void findsById() {
        MarketListing saved = repository.saveListing(listing(1L, 10L, 100));

        Assertions.assertTrue(repository.findById(saved.id()).isPresent());
        Assertions.assertTrue(repository.findById(999L).isEmpty());
    }

    /**
     * Проверяет, что активные лоты отсортированы по идентификатору.
     */
    @Test
    @DisplayName("Возвращает активные лоты по порядку")
    void returnsActiveListingsSorted() {
        repository.saveListing(listing(1L, 10L, 100));
        repository.saveListing(listing(2L, 11L, 200));

        List<MarketListing> active = repository.findAllActive();

        Assertions.assertEquals(2, active.size());
        Assertions.assertEquals(1L, active.get(0).id());
        Assertions.assertEquals(2L, active.get(1).id());
    }

    /**
     * Проверяет поиск лотов продавца.
     */
    @Test
    @DisplayName("Находит лоты продавца")
    void findsBySeller() {
        repository.saveListing(listing(1L, 10L, 100));
        repository.saveListing(listing(2L, 11L, 200));
        repository.saveListing(listing(1L, 12L, 300));

        List<MarketListing> listings = repository.findBySeller(1L);

        Assertions.assertEquals(2, listings.size());
        Assertions.assertTrue(listings.stream().allMatch(item -> item.sellerId() == 1L));
    }

    /**
     * Проверяет удаление лота с возвратом.
     */
    @Test
    @DisplayName("Удаляет лот и возвращает его")
    void deletesListingAndReturns() {
        MarketListing saved = repository.saveListing(listing(1L, 10L, 100));

        var deleted = repository.deleteListingAndReturn(null, saved.id());

        Assertions.assertTrue(deleted.isPresent());
        Assertions.assertEquals(saved.id(), deleted.get().id());
        Assertions.assertTrue(repository.findById(saved.id()).isEmpty());
    }

    /**
     * Проверяет, что удаление отсутствующего лота возвращает пустой результат.
     */
    @Test
    @DisplayName("Возвращает пустой результат для отсутствующего лота")
    void returnsEmptyForMissingListing() {
        Assertions.assertTrue(repository.deleteListingAndReturn(null, 999L).isEmpty());
    }

    /**
     * Проверяет удаление лота только его продавцом.
     */
    @Test
    @DisplayName("Удаляет лот только правильным продавцом")
    void deletesBySellerOnly() {
        MarketListing saved = repository.saveListing(listing(1L, 10L, 100));

        var wrongSeller = repository.deleteListingBySellerAndReturn(null, saved.id(), 2L);
        var rightSeller = repository.deleteListingBySellerAndReturn(null, saved.id(), 1L);

        Assertions.assertTrue(wrongSeller.isEmpty());
        Assertions.assertTrue(rightSeller.isPresent());
        Assertions.assertTrue(repository.findById(saved.id()).isEmpty());
    }
}
