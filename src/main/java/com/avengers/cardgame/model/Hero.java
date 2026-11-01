package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Карточка героя.
 *
 * @param id уникальный идентификатор
 * @param name имя героя
 * @param rarity редкость
 * @param attack сила атаки
 * @param defense сила защиты
 * @param health здоровье
 * @param description описание героя (может быть пустым)
 */
public record Hero(
        long id,
        @NotNull String name,
        @NotNull HeroRarity rarity,
        int attack,
        int defense,
        int health,
        @NotNull String description
) {
    public Hero {
        Objects.requireNonNull(name, "Hero name must not be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Hero name must not be empty");
        }

        Objects.requireNonNull(rarity, "Hero rarity must not be null");

        if (attack < 0 || defense < 0 || health < 0) {
            throw new IllegalArgumentException("Hero stats must be non-negative");
        }

        Objects.requireNonNull(description, "Hero description must not be null");
    }
}