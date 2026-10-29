
package com.avengers.cardgame.model;

/**
 * Редкость карточки героя.
 */
public enum HeroRarity {
    /** Обычная редкость. */
    COMMON(60),
    /** Редкая редкость. */
    RARE(25),
    /** Эпическая редкость. */
    EPIC(12),
    /** Легендарная редкость. */
    LEGENDARY(3);

    /** Вес для выпадения. */
    private final int weight;

    /**
     * @param weight вес для выпадения
     */
    HeroRarity(int weight) {
        this.weight = weight;
    }

    /**
     * @return вес для выпадения
     */
    public int weight() {
        return weight;
    }
}
