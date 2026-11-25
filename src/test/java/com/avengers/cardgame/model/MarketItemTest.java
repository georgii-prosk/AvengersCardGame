package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link MarketItem}.
 */
class MarketItemTest {

    /** Лот для тестов. */
    private static final MarketListing LISTING =
            new MarketListing(1L, 2L, 3L, 100, LocalDateTime.of(2026, 1, 1, 12, 0));

    /** Герой для тестов. */
    private static final Hero HERO = new Hero(3L, "Thor", HeroRarity.EPIC, 10, 5, 20, "desc");

    /**
     * Проверяет создание элемента рынка с валидными данными.
     */
    @Test
    @DisplayName("Создаёт элемент рынка с валидными данными")
    void createsValidItem() {
        MarketItem item = new MarketItem(LISTING, HERO, "Tony");

        Assertions.assertEquals(LISTING, item.listing());
        Assertions.assertEquals(HERO, item.hero());
        Assertions.assertEquals("Tony", item.sellerName());
    }

    /**
     * Проверяет, что отсутствие лота отклоняется.
     */
    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Бросает исключение при отсутствии лота")
    void throwsOnNullListing() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarketItem(null, HERO, "Tony"));
    }

    /**
     * Проверяет, что отсутствие героя отклоняется.
     */
    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Бросает исключение при отсутствии героя")
    void throwsOnNullHero() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarketItem(LISTING, null, "Tony"));
    }

    /**
     * Проверяет, что отсутствие имени продавца отклоняется.
     */
    @Test
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Бросает исключение при отсутствии имени продавца")
    void throwsOnNullSellerName() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new MarketItem(LISTING, HERO, null));
    }

    /**
     * Проверяет, что пустое имя продавца отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при пустом имени продавца")
    void throwsOnBlankSellerName() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new MarketItem(LISTING, HERO, "  "));
    }
}
