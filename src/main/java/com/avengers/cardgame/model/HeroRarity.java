package com.avengers.cardgame.model;

/**
 * Редкость карточки героя и её экономические параметры.
 */
public enum HeroRarity {
    /** Обычная редкость. */
    COMMON(1, 5, 10, 200),
    /** Редкая редкость. */
    RARE(3, 15, 50, 1_000),
    /** Эпическая редкость. */
    EPIC(8, 40, 200, 5_000),
    /** Легендарная редкость. */
    LEGENDARY(20, 100, 1_000, 25_000);

    /** Количество кусочков, получаемых при разборе одной карточки. */
    private final int shardsYield;

    /** Стоимость крафта одной карточки в кусочках. */
    private final int craftCost;

    /** Минимально допустимая цена лота на рынке. */
    private final int minPrice;

    /** Максимально допустимая цена лота на рынке. */
    private final int maxPrice;

    /**
     * @param shardsYield количество кусочков за разбор карточки
     * @param craftCost   стоимость крафта карточки
     * @param minPrice    минимальная цена лота
     * @param maxPrice    максимальная цена лота
     */
    HeroRarity(int shardsYield, int craftCost, int minPrice, int maxPrice) {
        this.shardsYield = shardsYield;
        this.craftCost = craftCost;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    /**
     * @return количество кусочков за разбор одной карточки
     */
    public int shardsYield() {
        return shardsYield;
    }

    /**
     * @return стоимость крафта карточки в кусочках
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * @return минимально допустимая цена лота
     */
    public int minPrice() {
        return minPrice;
    }

    /**
     * @return максимально допустимая цена лота
     */
    public int maxPrice() {
        return maxPrice;
    }
}
