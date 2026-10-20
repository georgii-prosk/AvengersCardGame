package com.avengers.cardgame.model;

/**
 * Карточка героя.
 *
 * @param id          уникальный идентификатор
 * @param name        имя героя
 * @param rarity      редкость
 * @param atk         атака
 * @param def         защита
 * @param hp          здоровье
 * @param description короткое описание
 */
public record Hero(
        long id,
        String name,
        HeroRarity rarity,
        int atk,
        int def,
        int hp,
        String description
) {
    /**
     * Создаёт карточку с проверкой инвариантов.
     *
     * @throws IllegalArgumentException если статы отрицательные или имя пустое
     */
    public Hero {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hero name must not be empty");
        }
        if (atk < 0 || def < 0 || hp < 0) {
            throw new IllegalArgumentException("Hero stats must be non-negative");
        }
    }
}