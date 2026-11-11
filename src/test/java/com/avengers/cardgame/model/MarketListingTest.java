package com.avengers.cardgame.model;

import com.avengers.cardgame.support.NullParameterAssertion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link MarketListing}.
 */
class MarketListingTest {

    /** Время выставления лота, используемое в тестах. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    @Test
    @DisplayName("Создаёт лот с валидными данными")
    void createsValidListing() {
        MarketListing listing = new MarketListing(1L, 2L, 3L, 200, CREATED_AT);

        Assertions.assertEquals(1L, listing.id());
        Assertions.assertEquals(2L, listing.sellerId());
        Assertions.assertEquals(3L, listing.heroId());
        Assertions.assertEquals(200, listing.price());
        Assertions.assertEquals(CREATED_AT, listing.createdAt());
    }

    @Test
    @DisplayName("Допускает нулевой идентификатор для нового лота")
    void allowsZeroIdForNewListing() {
        MarketListing listing = new MarketListing(0L, 2L, 3L, 200, CREATED_AT);

        Assertions.assertEquals(0L, listing.id());
    }

    @Test
    @DisplayName("Бросает исключение при нулевой цене")
    void throwsOnZeroPrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(1L, 2L, 3L, 0, CREATED_AT));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательной цене")
    void throwsOnNegativePrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(1L, 2L, 3L, -1, CREATED_AT));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо даты создания")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullCreatedAt() {
        NullParameterAssertion.assertRejected(
                () -> new MarketListing(1L, 2L, 3L, 200, null));
    }
}
