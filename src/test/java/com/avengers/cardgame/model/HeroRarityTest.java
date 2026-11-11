package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты экономических параметров {@link HeroRarity}.
 */
class HeroRarityTest {

    /**
     * Проверяет параметры одной редкости.
     *
     * @param rarity      редкость
     * @param shardsYield ожидаемый выход кусочков
     * @param craftCost   ожидаемая стоимость крафта
     * @param minPrice    ожидаемая минимальная цена
     * @param maxPrice    ожидаемая максимальная цена
     */
    private void assertParameters(HeroRarity rarity, int shardsYield, int craftCost,
                                  int minPrice, int maxPrice) {
        Assertions.assertEquals(shardsYield, rarity.shardsYield(), rarity + " shardsYield");
        Assertions.assertEquals(craftCost, rarity.craftCost(), rarity + " craftCost");
        Assertions.assertEquals(minPrice, rarity.minPrice(), rarity + " minPrice");
        Assertions.assertEquals(maxPrice, rarity.maxPrice(), rarity + " maxPrice");
    }

    @Test
    @DisplayName("Обычная редкость: 1 кусочек, крафт 5, цена 10–200")
    void commonParameters() {
        assertParameters(HeroRarity.COMMON, 1, 5, 10, 200);
    }

    @Test
    @DisplayName("Редкая редкость: 3 кусочка, крафт 15, цена 50–1000")
    void rareParameters() {
        assertParameters(HeroRarity.RARE, 3, 15, 50, 1_000);
    }

    @Test
    @DisplayName("Эпическая редкость: 8 кусочков, крафт 40, цена 200–5000")
    void epicParameters() {
        assertParameters(HeroRarity.EPIC, 8, 40, 200, 5_000);
    }

    @Test
    @DisplayName("Легендарная редкость: 20 кусочков, крафт 100, цена 1000–25000")
    void legendaryParameters() {
        assertParameters(HeroRarity.LEGENDARY, 20, 100, 1_000, 25_000);
    }

    @Test
    @DisplayName("Диапазоны цен корректны и растут вместе с редкостью")
    void priceRangesAreValidAndOrdered() {
        HeroRarity[] rarities = HeroRarity.values();
        for (HeroRarity rarity : rarities) {
            Assertions.assertTrue(rarity.minPrice() < rarity.maxPrice(), rarity.name());
            Assertions.assertTrue(rarity.shardsYield() > 0, rarity.name());
            Assertions.assertTrue(rarity.craftCost() >= rarity.shardsYield(), rarity.name());
        }
        for (int index = 1; index < rarities.length; index++) {
            Assertions.assertTrue(rarities[index].minPrice() > rarities[index - 1].minPrice());
            Assertions.assertTrue(rarities[index].maxPrice() > rarities[index - 1].maxPrice());
        }
    }
}
