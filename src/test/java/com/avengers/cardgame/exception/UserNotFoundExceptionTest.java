package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link UserNotFoundException}.
 */
class UserNotFoundExceptionTest {

    @Test
    @DisplayName("Сообщение содержит идентификатор пользователя")
    void messageContainsUserId() {
        UserNotFoundException ex = new UserNotFoundException(42L);

        Assertions.assertTrue(ex.getMessage().contains("42"));
    }

    @Test
    @DisplayName("userId() возвращает переданный идентификатор")
    void userIdReturnsCorrectValue() {
        UserNotFoundException ex = new UserNotFoundException(123L);

        Assertions.assertEquals(123L, ex.userId());
    }

    @Test
    @DisplayName("Является RuntimeException")
    void isRuntimeException() {
        UserNotFoundException ex = new UserNotFoundException(1L);

        Assertions.assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    @DisplayName("Работает с большим идентификатором")
    void worksWithLargeUserId() {
        long bigId = 9_999_999_999_999L;
        UserNotFoundException ex = new UserNotFoundException(bigId);

        Assertions.assertEquals(bigId, ex.userId());
        Assertions.assertTrue(ex.getMessage().contains(String.valueOf(bigId)));
    }

    @Test
    @DisplayName("Работает с нулевым идентификатором")
    void worksWithZeroUserId() {
        UserNotFoundException ex = new UserNotFoundException(0L);

        Assertions.assertEquals(0L, ex.userId());
    }
}