package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: цена лота выходит за допустимые пределы редкости.
 */
public class InvalidPriceException extends AppException {

    /** Указанная цена. */
    private final int price;

    /** Минимально допустимая цена. */
    private final int minPrice;

    /** Максимально допустимая цена. */
    private final int maxPrice;

    /**
     * @param price    указанная цена
     * @param minPrice минимально допустимая цена
     * @param maxPrice максимально допустимая цена
     */
    public InvalidPriceException(int price, int minPrice, int maxPrice) {
        super("Invalid price: price=" + price + ", allowed=" + minPrice + ".." + maxPrice);
        this.price = price;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    /**
     * @return указанная цена
     */
    public int price() {
        return price;
    }

    /**
     * @return минимально допустимая цена
     */
    public int minPrice() {
        return minPrice;
    }

    /**
     * @return максимально допустимая цена
     */
    public int maxPrice() {
        return maxPrice;
    }
}
