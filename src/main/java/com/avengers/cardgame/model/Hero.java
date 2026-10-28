package com.avengers.cardgame.model;

public record Hero(long id, String name, HeroRarity rarity, int attack, int defense, int health, String description) {
    public Hero {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Hero name must not be empty");
        if (rarity == null) throw new IllegalArgumentException("Hero rarity must not be null");
        if (attack < 0 || defense < 0 || health < 0) throw new IllegalArgumentException("Hero stats must be non-negative");
    }
}
