package com.avengers.cardgame.model;

/**
 * Редкость карточки героя.
 */
public enum HeroRarity {
    /** Обычная редкость. */
    COMMON(60, "⚪"),
    /** Редкая редкость. */
    RARE(25, "🔵"),
    /** Эпическая редкость. */
    EPIC(12, "🟣"),
    /** Легендарная редкость. */
    LEGENDARY(3, "🟡");

    /** Вес для выпадения. */
    private final int weight;

    /** Эмодзи для отображения. */
    private final String emoji;

    /**
     * @param weight вес для выпадения
     * @param emoji  эмодзи редкости
     */
    HeroRarity(int weight, String emoji) {
        this.weight = weight;
        this.emoji = emoji;
    }

    /**
     * @return вес для выпадения
     */
    public int weight() {
        return weight;
    }

    /**
     * @return эмодзи редкости
     */
    public String emoji() {
        return emoji;
    }
}