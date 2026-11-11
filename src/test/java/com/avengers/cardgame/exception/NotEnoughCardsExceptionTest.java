package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link NotEnoughCardsException}.
 */
class NotEnoughCardsExceptionTest {

    @Test
    @DisplayName("Сообщение содержит все параметры ошибки")
    void messageContainsAllParameters() {
        NotEnoughCardsException exception = new NotEnoughCardsException(1L, 2L, 5, 2);

        Assertions.assertTrue(exception.getMessage().contains("userId=1"));
        Assertions.assertTrue(exception.getMessage().contains("heroId=2"));
        Assertions.assertTrue(exception.getMessage().contains("requested=5"));
        Assertions.assertTrue(exception.getMessage().contains("available=2"));
    }

    @Test
    @DisplayName("Аксессоры возвращают переданные значения")
    void accessorsReturnValues() {
        NotEnoughCardsException exception = new NotEnoughCardsException(10L, 20L, 3, 1);

        Assertions.assertEquals(10L, exception.userId());
        Assertions.assertEquals(20L, exception.heroId());
        Assertions.assertEquals(3, exception.requested());
        Assertions.assertEquals(1, exception.available());
    }
}
