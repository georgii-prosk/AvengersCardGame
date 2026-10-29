package com.avengers.cardgame.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Тесты для {@link Hero}.
 */
class HeroTest {

    /**
     * Проверяет создание героя с валидными данными.
     */
    @Test
    @DisplayName("Создаёт героя с валидными данными")
    void createsValidHero() {
        Hero hero = new Hero(1L, "Iron Man", HeroRarity.EPIC, 85, 70, 120, "Genius");

        Assertions.assertEquals(1L, hero.id());
        Assertions.assertEquals("Iron Man", hero.name());
        Assertions.assertEquals(HeroRarity.EPIC, hero.rarity());
        Assertions.assertEquals(85, hero.attack());
    }

    /**
     * Проверяет, что пустое имя отклоняется.
     *
     * @param name пустое имя
     */
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Бросает исключение при пустом имени")
    void throwsOnBlankName(String name) {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hero(1L, name, HeroRarity.COMMON, 10, 10, 10, null));
    }

    /**
     * Проверяет, что отрицательная атака отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательной атаке")
    void throwsOnNegativeAtk() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hero(1L, "Hero", HeroRarity.COMMON, -1, 10, 10, null));
    }

    /**
     * Проверяет, что отрицательная защита отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательной защите")
    void throwsOnNegativeDef() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hero(1L, "Hero", HeroRarity.COMMON, 10, -1, 10, null));
    }

    /**
     * Проверяет, что отрицательное здоровье отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательном здоровье")
    void throwsOnNegativeHp() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Hero(1L, "Hero", HeroRarity.COMMON, 10, 10, -1, null));
    }
}