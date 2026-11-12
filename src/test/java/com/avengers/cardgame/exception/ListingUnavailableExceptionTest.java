package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link ListingUnavailableException}.
 */
class ListingUnavailableExceptionTest {

    /**
     * Проверяет, что геттер возвращает идентификатор лота.
     */
    @Test
    @DisplayName("listingId() возвращает переданный идентификатор")
    void listingIdReturnsValue() {
        ListingUnavailableException exception = new ListingUnavailableException(42L);

        Assertions.assertEquals(42L, exception.listingId());
    }

    /**
     * Проверяет, что сообщение содержит идентификатор лота.
     */
    @Test
    @DisplayName("Сообщение содержит идентификатор лота")
    void messageContainsListingId() {
        ListingUnavailableException exception = new ListingUnavailableException(42L);

        Assertions.assertTrue(exception.getMessage().contains("42"));
    }

    /**
     * Проверяет, что исключение является проверяемым наследником {@link AppException}.
     */
    @Test
    @DisplayName("Наследует AppException")
    void extendsAppException() {
        Assertions.assertInstanceOf(AppException.class, new ListingUnavailableException(42L));
    }
}
