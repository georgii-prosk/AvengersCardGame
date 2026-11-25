package com.avengers.cardgame.dto;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link PurchaseResult}.
 */
class PurchaseResultTest {

    /**
     * Проверяет создание результата покупки с валидными данными.
     */
    @Test
    @DisplayName("Создаёт результат покупки с валидными данными")
    void createsValidResult() {
        PurchaseResult result = new PurchaseResult(1L, 2L, 3L, 100);

        Assertions.assertEquals(1L, result.buyerId());
        Assertions.assertEquals(2L, result.sellerId());
        Assertions.assertEquals(3L, result.heroId());
        Assertions.assertEquals(100, result.price());
    }

    /**
     * Проверяет, что неположительная цена отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительной цене")
    void throwsOnNonPositivePrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PurchaseResult(1L, 2L, 3L, 0));
    }

    /**
     * Проверяет, что отрицательная цена отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательной цене")
    void throwsOnNegativePrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PurchaseResult(1L, 2L, 3L, -10));
    }
}
