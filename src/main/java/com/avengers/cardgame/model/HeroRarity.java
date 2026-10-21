package com.avengers.cardgame.model;

/**
 * Редкость карточки героя.
 */
public enum HeroRarity {
    /** Обычная редкость. */
    COMMON(60, "⚪", "Обычная"),
    /** Редкая редкость. */
    RARE(25, "🔵", "Редкая"),
    /** Эпическая редкость. */
    EPIC(12, "🟣", "Эпическая"),
    /** Легендарная редкость. */
    LEGENDARY(3, "🟡", "Легендарная");

    /** Вес для выпадения. */
    private final int weight;

    /** Эмодзи для отображения. */
    private final String emoji;

    /** Название для отображения пользователю. */
    private final String title;

    /**
     * @param weight вес для выпадения
     * @param emoji  эмодзи редкости
     * @param title  название редкости для пользователя
     */
    HeroRarity(int weight, String emoji, String title) {
        this.weight = weight;
        this.emoji = emoji;
        this.title = title;
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

    /**
     * @return название редкости для отображения пользователю
     */
    public String title() {
        return title;
    }
}