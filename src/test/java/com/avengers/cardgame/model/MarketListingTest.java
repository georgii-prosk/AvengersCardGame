package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link MarketListing}.
 */
class MarketListingTest {

    /** Время создания лота для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /**
     * Проверяет создание лота с валидными данными.
     */
    @Test
    @DisplayName("Создаёт лот с валидными данными")
    void createsValidListing() {
        MarketListing listing = new MarketListing(1L, 2L, 3L, 100, CREATED_AT);

        Assertions.assertEquals(1L, listing.id());
        Assertions.assertEquals(2L, listing.sellerId());
        Assertions.assertEquals(3L, listing.heroId());
        Assertions.assertEquals(100, listing.price());
        Assertions.assertEquals(CREATED_AT, listing.createdAt());
    }

    /**
     * Проверяет, что нулевой идентификатор допустим для ещё не сохранённого лота.
     */
    @Test
    @DisplayName("Допускает нулевой идентификатор")
    void allowsZeroId() {
        MarketListing listing = new MarketListing(0L, 2L, 3L, 100, CREATED_AT);

        Assertions.assertEquals(0L, listing.id());
    }

    /**
     * Проверяет, что отрицательный идентификатор отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательном идентификаторе")
    void throwsOnNegativeId() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(-1L, 2L, 3L, 100, CREATED_AT));
    }

    /**
     * Проверяет, что неположительный идентификатор продавца отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительном продавце")
    void throwsOnNonPositiveSellerId() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(1L, 0L, 3L, 100, CREATED_AT));
    }

    /**
     * Проверяет, что неположительный идентификатор героя отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительном герое")
    void throwsOnNonPositiveHeroId() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(1L, 2L, 0L, 100, CREATED_AT));
    }

    /**
     * Проверяет, что неположительная цена отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительной цене")
    void throwsOnNonPositivePrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketListing(1L, 2L, 3L, 0, CREATED_AT));
    }

    /**
     * Проверяет, что отсутствие времени создания отклоняется.
     */
    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Бросает исключение при отсутствии времени создания")
    void throwsOnNullCreatedAt() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarketListing(1L, 2L, 3L, 100, null));
    }
}
