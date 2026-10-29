package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * Тесты для {@link HeroRarity}.
 */
class HeroRarityTest {

    /**
     * Проверяет, что сумма весов всех редкостей равна 100.
     */
    @Test
    @DisplayName("Сумма весов всех редкостей равна 100")
    void totalWeightIsHundred() {
        int totalWeight = Arrays.stream(HeroRarity.values())
                .mapToInt(HeroRarity::weight)
                .sum();

        Assertions.assertEquals(100, totalWeight);
    }
}
