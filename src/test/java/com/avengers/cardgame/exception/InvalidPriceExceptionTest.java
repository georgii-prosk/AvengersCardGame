package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InvalidPriceException}.
 */
class InvalidPriceExceptionTest {

    /**
     * Проверяет, что геттеры возвращают переданные значения.
     */
    @Test
    @DisplayName("Геттеры возвращают переданные значения")
    void gettersReturnValues() {
        InvalidPriceException exception = new InvalidPriceException(5, 10, 200);

        Assertions.assertEquals(5, exception.price());
        Assertions.assertEquals(10, exception.minPrice());
        Assertions.assertEquals(200, exception.maxPrice());
    }

    /**
     * Проверяет, что сообщение содержит цену и допустимый диапазон.
     */
    @Test
    @DisplayName("Сообщение содержит цену и диапазон")
    void messageContainsPriceRange() {
        InvalidPriceException exception = new InvalidPriceException(5, 10, 200);

        Assertions.assertTrue(exception.getMessage().contains("5"));
        Assertions.assertTrue(exception.getMessage().contains("10"));
        Assertions.assertTrue(exception.getMessage().contains("200"));
    }

    /**
     * Проверяет, что исключение является проверяемым наследником {@link AppException}.
     */
    @Test
    @DisplayName("Наследует AppException")
    void extendsAppException() {
        Assertions.assertInstanceOf(AppException.class, new InvalidPriceException(5, 10, 200));
    }
}
