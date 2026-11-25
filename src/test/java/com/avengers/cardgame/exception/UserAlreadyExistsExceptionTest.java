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
        UserAlreadyExistsException ex = new UserAlreadyExistsException(42L);

        Assertions.assertEquals("User already exists: userId=42", ex.getMessage());
    }

    @Test
    @DisplayName("userId() возвращает переданный идентификатор")
    void userIdReturnsCorrectValue() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException(123L);

        Assertions.assertEquals(123L, ex.userId());
    }

    @Test
    @DisplayName("Работает с большим идентификатором")
    void worksWithLargeUserId() {
        long bigId = 9_999_999_999_999L;
        UserAlreadyExistsException ex = new UserAlreadyExistsException(bigId);

        Assertions.assertEquals(bigId, ex.userId());
        Assertions.assertEquals("User already exists: userId=" + bigId, ex.getMessage());
    }

    @Test
    @DisplayName("Работает с нулевым идентификатором")
    void worksWithZeroUserId() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException(0L);

        Assertions.assertEquals(0L, ex.userId());
    }
}