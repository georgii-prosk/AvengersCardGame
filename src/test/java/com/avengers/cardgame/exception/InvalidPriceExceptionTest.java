package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InvalidPriceException}.
 */
class InvalidPriceExceptionTest {

    @Test
    @DisplayName("Сообщение содержит цену и допустимый диапазон")
    void messageContainsPriceRange() {
        InvalidPriceException exception = new InvalidPriceException(5, 10, 200);

        Assertions.assertTrue(exception.getMessage().contains("price=5"));
        Assertions.assertTrue(exception.getMessage().contains("10..200"));
    }

    @Test
    @DisplayName("Аксессоры возвращают переданные значения")
    void accessorsReturnValues() {
        InvalidPriceException exception = new InvalidPriceException(250, 10, 200);

        Assertions.assertEquals(250, exception.price());
        Assertions.assertEquals(10, exception.minPrice());
        Assertions.assertEquals(200, exception.maxPrice());
    }
}
