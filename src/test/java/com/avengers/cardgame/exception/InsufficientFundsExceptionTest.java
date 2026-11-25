package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InsufficientFundsException}.
 */
class InsufficientFundsExceptionTest {

    /**
     * Проверяет, что геттеры возвращают переданные значения.
     */
    @Test
    @DisplayName("Геттеры возвращают переданные значения")
    void gettersReturnValues() {
        InsufficientFundsException exception = new InsufficientFundsException(100, 30);

        Assertions.assertEquals(100, exception.required());
        Assertions.assertEquals(30, exception.available());
        Assertions.assertEquals(70, exception.missing());
    }

    /**
     * Проверяет, что недостающее количество вычисляется как разница.
     */
    @Test
    @DisplayName("Недостающее количество вычисляется как разница")
    void missingUsesDifference() {
        InsufficientFundsException exception = new InsufficientFundsException(10, 30);

        Assertions.assertEquals(-20, exception.missing());
    }

    /**
     * Проверяет, что сообщение содержит требуемое и доступное количество.
     */
    @Test
    @DisplayName("Сообщение содержит детали ошибки")
    void messageContainsDetails() {
        InsufficientFundsException exception = new InsufficientFundsException(100, 30);

        Assertions.assertEquals("Insufficient funds: required=100, available=30",
                exception.getMessage());
    }
}
