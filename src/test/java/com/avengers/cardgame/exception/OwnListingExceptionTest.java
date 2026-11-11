package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link OwnListingException}.
 */
class OwnListingExceptionTest {

    /**
     * Проверяет, что геттер возвращает идентификатор лота.
     */
    @Test
    @DisplayName("listingId() возвращает переданный идентификатор")
    void listingIdReturnsValue() {
        OwnListingException exception = new OwnListingException(42L);

        Assertions.assertEquals(42L, exception.listingId());
    }

    /**
     * Проверяет, что сообщение содержит идентификатор лота.
     */
    @Test
    @DisplayName("Сообщение содержит идентификатор лота")
    void messageContainsListingId() {
        OwnListingException exception = new OwnListingException(42L);

        Assertions.assertTrue(exception.getMessage().contains("42"));
    }
}
