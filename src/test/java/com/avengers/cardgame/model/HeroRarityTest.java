package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link HeroRarity}.
 */
class HeroRarityTest {

    /**
     * Проверяет экономические параметры обычной редкости.
     */
    @Test
    @DisplayName("Обычная редкость имеет корректные параметры")
    void commonHasExpectedParameters() {
        Assertions.assertEquals(1, HeroRarity.COMMON.shardsYield());
        Assertions.assertEquals(5, HeroRarity.COMMON.craftCost());
        Assertions.assertEquals(10, HeroRarity.COMMON.minPrice());
        Assertions.assertEquals(200, HeroRarity.COMMON.maxPrice());
        Assertions.assertEquals("Common", HeroRarity.COMMON.displayName());
    }

    /**
     * Проверяет экономические параметры редкой редкости.
     */
    @Test
    @DisplayName("Редкая редкость имеет корректные параметры")
    void rareHasExpectedParameters() {
        Assertions.assertEquals(3, HeroRarity.RARE.shardsYield());
        Assertions.assertEquals(15, HeroRarity.RARE.craftCost());
        Assertions.assertEquals(50, HeroRarity.RARE.minPrice());
        Assertions.assertEquals(1000, HeroRarity.RARE.maxPrice());
        Assertions.assertEquals("Rare", HeroRarity.RARE.displayName());
    }

    /**
     * Проверяет экономические параметры эпической редкости.
     */
    @Test
    @DisplayName("Эпическая редкость имеет корректные параметры")
    void epicHasExpectedParameters() {
        Assertions.assertEquals(8, HeroRarity.EPIC.shardsYield());
        Assertions.assertEquals(40, HeroRarity.EPIC.craftCost());
        Assertions.assertEquals(200, HeroRarity.EPIC.minPrice());
        Assertions.assertEquals(5000, HeroRarity.EPIC.maxPrice());
        Assertions.assertEquals("Epic", HeroRarity.EPIC.displayName());
    }

    /**
     * Проверяет экономические параметры легендарной редкости.
     */
    @Test
    @DisplayName("Легендарная редкость имеет корректные параметры")
    void legendaryHasExpectedParameters() {
        Assertions.assertEquals(20, HeroRarity.LEGENDARY.shardsYield());
        Assertions.assertEquals(100, HeroRarity.LEGENDARY.craftCost());
        Assertions.assertEquals(1000, HeroRarity.LEGENDARY.minPrice());
        Assertions.assertEquals(25000, HeroRarity.LEGENDARY.maxPrice());
        Assertions.assertEquals("Legendary", HeroRarity.LEGENDARY.displayName());
    }

    /**
     * Проверяет, что с ростом редкости растут все экономические показатели.
     */
    @Test
    @DisplayName("Параметры возрастают вместе с редкостью")
    void parametersGrowWithRarity() {
        HeroRarity[] values = HeroRarity.values();
        for (int index = 1; index < values.length; index++) {
            HeroRarity previous = values[index - 1];
            HeroRarity current = values[index];

            Assertions.assertTrue(current.shardsYield() > previous.shardsYield());
            Assertions.assertTrue(current.craftCost() > previous.craftCost());
            Assertions.assertTrue(current.minPrice() > previous.minPrice());
            Assertions.assertTrue(current.maxPrice() > previous.maxPrice());
        }
    }

    /**
     * Проверяет, что минимальная цена не превышает максимальную для каждой редкости.
     */
    @Test
    @DisplayName("Минимальная цена не превышает максимальную")
    void minPriceDoesNotExceedMaxPrice() {
        for (HeroRarity rarity : HeroRarity.values()) {
            Assertions.assertTrue(rarity.minPrice() <= rarity.maxPrice());
        }
    }
}
