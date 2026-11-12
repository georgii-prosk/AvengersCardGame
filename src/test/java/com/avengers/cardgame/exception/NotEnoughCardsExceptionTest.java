package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link NotEnoughCardsException}.
 */
class NotEnoughCardsExceptionTest {

    /**
     * Проверяет, что сообщение содержит запрошенное и доступное количество.
     */
    @Test
    @DisplayName("Сообщение содержит детали ошибки")
    void messageContainsDetails() {
        NotEnoughCardsException exception = new NotEnoughCardsException(5L, 3, 1);

        Assertions.assertTrue(exception.getMessage().contains("5"));
        Assertions.assertTrue(exception.getMessage().contains("3"));
        Assertions.assertTrue(exception.getMessage().contains("1"));
    }

    /**
     * Проверяет, что геттеры возвращают переданные значения.
     */
    @Test
    @DisplayName("Геттеры возвращают переданные значения")
    void gettersReturnValues() {
        NotEnoughCardsException exception = new NotEnoughCardsException(5L, 3, 1);

        Assertions.assertEquals(5L, exception.heroId());
        Assertions.assertEquals(3, exception.requested());
        Assertions.assertEquals(1, exception.available());
    }

    /**
     * Проверяет, что исключение является проверяемым наследником {@link AppException}.
     */
    @Test
    @DisplayName("Наследует AppException")
    void extendsAppException() {
        Assertions.assertInstanceOf(AppException.class, new NotEnoughCardsException(5L, 3, 1));
    }
}
