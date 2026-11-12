package com.avengers.cardgame.model;

/**
 * Редкость карточки героя и связанные с ней экономические параметры.
 */
public enum HeroRarity {
    /** Обычная редкость. */
    COMMON(1, 5, 10, 200, "Common"),
    /** Редкая редкость. */
    RARE(3, 15, 50, 1000, "Rare"),
    /** Эпическая редкость. */
    EPIC(8, 40, 200, 5000, "Epic"),
    /** Легендарная редкость. */
    LEGENDARY(20, 100, 1000, 25000, "Legendary");

    /** Количество кусочков, получаемых за разбор одной карточки. */
    private final int shardsYield;

    /** Количество кусочков, необходимое для крафта одной карточки. */
    private final int craftCost;

    /** Минимально допустимая цена лота. */
    private final int minPrice;

    /** Максимально допустимая цена лота. */
    private final int maxPrice;

    /** Название редкости для отображения в интерфейсе. */
    private final String displayName;

    /**
     * Создаёт описание экономических параметров редкости.
     *
     * @param shardsYield количество кусочков за разбор карточки
     * @param craftCost   стоимость крафта в кусочках
     * @param minPrice    минимальная цена лота
     * @param maxPrice    максимальная цена лота
     * @param displayName название редкости для интерфейса
     */
    HeroRarity(int shardsYield, int craftCost, int minPrice, int maxPrice, String displayName) {
        this.shardsYield = shardsYield;
        this.craftCost = craftCost;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.displayName = displayName;
    }

    /**
     * @return количество кусочков, получаемых за разбор одной карточки
     */
    public int shardsYield() {
        return shardsYield;
    }

    /**
     * @return количество кусочков, необходимое для крафта одной карточки
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

    /**
     * @return название редкости для отображения в интерфейсе
     */
    public String displayName() {
        return displayName;
    }
}
