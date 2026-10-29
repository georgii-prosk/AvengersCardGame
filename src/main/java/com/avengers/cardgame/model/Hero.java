package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Hero(
        long id,
        @NotNull String name,
        @NotNull HeroRarity rarity,
        int attack,
        int defense,
        int health,
        @Nullable String description
) {
    public Hero {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hero name must not be empty");
        }
        if (rarity == null) {
            throw new IllegalArgumentException("Hero rarity must not be null");
        }
        if (attack < 0 || defense < 0 || health < 0) {
            throw new IllegalArgumentException("Hero stats must be non-negative");
        }
    }
}