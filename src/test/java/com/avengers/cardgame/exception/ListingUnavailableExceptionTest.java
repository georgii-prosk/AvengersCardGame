package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ListingUnavailableException}.
 */
class ListingUnavailableExceptionTest {

    @Test
    @DisplayName("Конструктор по умолчанию использует причину «listing not found»")
    void defaultReasonIsListingNotFound() {
        ListingUnavailableException exception = new ListingUnavailableException(7L);

        Assertions.assertEquals(7L, exception.listingId());
        Assertions.assertEquals("listing not found", exception.reason());
        Assertions.assertTrue(exception.getMessage().contains("id=7"));
    }

    @Test
    @DisplayName("Конструктор с причиной сохраняет её")
    void customReasonIsStored() {
        ListingUnavailableException exception = new ListingUnavailableException(9L, "cannot buy own listing");

        Assertions.assertEquals(9L, exception.listingId());
        Assertions.assertEquals("cannot buy own listing", exception.reason());
        Assertions.assertTrue(exception.getMessage().contains("cannot buy own listing"));
    }
}
