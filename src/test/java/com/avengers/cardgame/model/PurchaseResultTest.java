package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link PurchaseResult}.
 */
class PurchaseResultTest {

    @Test
    @DisplayName("Создаёт результат покупки с валидными данными")
    void createsValidResult() {
        PurchaseResult result = new PurchaseResult(1L, 2L, 3L, 500);

        Assertions.assertEquals(1L, result.buyerId());
        Assertions.assertEquals(2L, result.sellerId());
        Assertions.assertEquals(3L, result.heroId());
        Assertions.assertEquals(500, result.price());
    }

    @Test
    @DisplayName("Бросает исключение при нулевой цене")
    void throwsOnZeroPrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PurchaseResult(1L, 2L, 3L, 0));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательной цене")
    void throwsOnNegativePrice() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new PurchaseResult(1L, 2L, 3L, -10));
    }
}
