package com.avengers.cardgame.model;

import com.avengers.cardgame.support.NullParameterAssertion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link MarketItem}.
 */
class MarketItemTest {

    /** Лот, используемый в тестах. */
    private final MarketListing listing = new MarketListing(
            1L, 2L, 3L, 500, LocalDateTime.of(2026, 1, 1, 12, 0));

    /** Герой, используемый в тестах. */
    private final Hero hero = new Hero(3L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    @Test
    @DisplayName("Создаёт элемент рынка с валидными данными")
    void createsValidItem() {
        MarketItem item = new MarketItem(listing, hero, "Tony");

        Assertions.assertEquals(listing, item.listing());
        Assertions.assertEquals(hero, item.hero());
        Assertions.assertEquals("Tony", item.sellerName());
    }

    @Test
    @DisplayName("Бросает исключение при null вместо лота")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullListing() {
        NullParameterAssertion.assertRejected(() -> new MarketItem(null, hero, "Tony"));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо героя")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullHero() {
        NullParameterAssertion.assertRejected(() -> new MarketItem(listing, null, "Tony"));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо имени продавца")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullSellerName() {
        NullParameterAssertion.assertRejected(() -> new MarketItem(listing, hero, null));
    }
}
