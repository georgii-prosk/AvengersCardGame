package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InsufficientFundsException}.
 */
class InsufficientFundsExceptionTest {

    @Test
    @DisplayName("Сообщение содержит требуемую и доступную сумму")
    void messageContainsAmounts() {
        InsufficientFundsException exception = new InsufficientFundsException(500, 100);

        Assertions.assertTrue(exception.getMessage().contains("required=500"));
        Assertions.assertTrue(exception.getMessage().contains("available=100"));
    }

    @Test
    @DisplayName("Аксессоры возвращают переданные значения")
    void accessorsReturnValues() {
        InsufficientFundsException exception = new InsufficientFundsException(200, 50);

        Assertions.assertEquals(200, exception.required());
        Assertions.assertEquals(50, exception.available());
    }
}
