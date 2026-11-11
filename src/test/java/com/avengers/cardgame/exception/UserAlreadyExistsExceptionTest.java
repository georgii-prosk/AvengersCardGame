package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link UserAlreadyExistsException}.
 */
class UserAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Сообщение содержит идентификатор пользователя")
    void messageContainsUserId() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException(42L);

        Assertions.assertTrue(exception.getMessage().contains("42"));
    }

    @Test
    @DisplayName("userId() возвращает переданный идентификатор")
    void userIdReturnsCorrectValue() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException(123L);

        Assertions.assertEquals(123L, exception.userId());
    }

    @Test
    @DisplayName("Работает с большим идентификатором")
    void worksWithLargeUserId() {
        long bigId = 9_999_999_999_999L;
        UserAlreadyExistsException exception = new UserAlreadyExistsException(bigId);

        Assertions.assertEquals(bigId, exception.userId());
        Assertions.assertTrue(exception.getMessage().contains(String.valueOf(bigId)));
    }

    @Test
    @DisplayName("Работает с нулевым идентификатором")
    void worksWithZeroUserId() {
        UserAlreadyExistsException exception = new UserAlreadyExistsException(0L);

        Assertions.assertEquals(0L, exception.userId());
    }
}