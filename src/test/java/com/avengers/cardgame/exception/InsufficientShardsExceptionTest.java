package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InsufficientShardsException}.
 */
class InsufficientShardsExceptionTest {

    @Test
    @DisplayName("Сообщение содержит требуемое и доступное количество")
    void messageContainsAmounts() {
        InsufficientShardsException exception = new InsufficientShardsException(40, 12);

        Assertions.assertTrue(exception.getMessage().contains("required=40"));
        Assertions.assertTrue(exception.getMessage().contains("available=12"));
    }

    @Test
    @DisplayName("Аксессоры возвращают переданные значения")
    void accessorsReturnValues() {
        InsufficientShardsException exception = new InsufficientShardsException(100, 0);

        Assertions.assertEquals(100, exception.required());
        Assertions.assertEquals(0, exception.available());
    }
}
