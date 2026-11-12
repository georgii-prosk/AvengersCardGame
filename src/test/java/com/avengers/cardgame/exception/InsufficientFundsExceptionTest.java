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
     * Проверяет, что недостающее количество не бывает отрицательным.
     */
    @Test
    @DisplayName("Недостающее количество не отрицательное")
    void missingIsNotNegative() {
        InsufficientFundsException exception = new InsufficientFundsException(10, 30);

        Assertions.assertEquals(0, exception.missing());
    }

    /**
     * Проверяет, что сообщение содержит требуемое и доступное количество.
     */
    @Test
    @DisplayName("Сообщение содержит детали ошибки")
    void messageContainsDetails() {
        InsufficientFundsException exception = new InsufficientFundsException(100, 30);

        Assertions.assertTrue(exception.getMessage().contains("100"));
        Assertions.assertTrue(exception.getMessage().contains("30"));
    }

    /**
     * Проверяет, что исключение является проверяемым наследником {@link AppException}.
     */
    @Test
    @DisplayName("Наследует AppException")
    void extendsAppException() {
        Assertions.assertInstanceOf(AppException.class, new InsufficientFundsException(100, 30));
    }
}
