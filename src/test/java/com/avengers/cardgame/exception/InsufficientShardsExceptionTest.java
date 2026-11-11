package com.avengers.cardgame.exception;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link InsufficientShardsException}.
 */
class InsufficientShardsExceptionTest {

    /**
     * Проверяет, что геттеры возвращают переданные значения.
     */
    @Test
    @DisplayName("Геттеры возвращают переданные значения")
    void gettersReturnValues() {
        InsufficientShardsException exception = new InsufficientShardsException(40, 15);

        Assertions.assertEquals(40, exception.required());
        Assertions.assertEquals(15, exception.available());
        Assertions.assertEquals(25, exception.missing());
    }

    /**
     * Проверяет, что недостающее количество не бывает отрицательным.
     */
    @Test
    @DisplayName("Недостающее количество не отрицательное")
    void missingIsNotNegative() {
        InsufficientShardsException exception = new InsufficientShardsException(10, 30);

        Assertions.assertEquals(0, exception.missing());
    }

    /**
     * Проверяет, что сообщение содержит требуемое и доступное количество.
     */
    @Test
    @DisplayName("Сообщение содержит детали ошибки")
    void messageContainsDetails() {
        InsufficientShardsException exception = new InsufficientShardsException(40, 15);

        Assertions.assertTrue(exception.getMessage().contains("40"));
        Assertions.assertTrue(exception.getMessage().contains("15"));
    }

    /**
     * Проверяет, что исключение является проверяемым наследником {@link AppException}.
     */
    @Test
    @DisplayName("Наследует AppException")
    void extendsAppException() {
        Assertions.assertInstanceOf(AppException.class, new InsufficientShardsException(40, 15));
    }
}
