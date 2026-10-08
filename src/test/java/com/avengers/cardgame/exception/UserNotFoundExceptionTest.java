package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link UserNotFoundException}.
 * <p>
 * Проверяют, что исключение несёт корректный Telegram ID
 * и формирует понятное сообщение.
 */
class UserNotFoundExceptionTest {

    @Test
    @DisplayName("Сообщение содержит Telegram ID")
    void messageContainsTelegramId() {
        UserNotFoundException ex = new UserNotFoundException(42L);

        Assertions.assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    @DisplayName("getTelegramId возвращает переданный ID")
    void getTelegramIdReturnsCorrectValue() {
        UserNotFoundException ex = new UserNotFoundException(123L);

        Assertions.assertEquals(123L, ex.getTelegramId());
    }

    @Test
    @DisplayName("Является RuntimeException (не checked)")
    void isRuntimeException() {
        UserNotFoundException ex = new UserNotFoundException(1L);

        Assertions.assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Работает с большим Telegram ID")
    void worksWithLargeTelegramId() {
        long bigId = 9_999_999_999_999L;
        UserNotFoundException ex = new UserNotFoundException(bigId);

        Assertions.assertEquals(bigId, ex.getTelegramId());
        Assertions.assertTrue(ex.getMessage().contains(String.valueOf(bigId)));
    }

    @Test
    @DisplayName("Работает с нулевым Telegram ID")
    void worksWithZeroTelegramId() {
        UserNotFoundException ex = new UserNotFoundException(0L);

        Assertions.assertEquals(0L, ex.getTelegramId());
    }
}