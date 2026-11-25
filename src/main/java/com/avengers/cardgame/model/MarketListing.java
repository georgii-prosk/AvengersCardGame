package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Лот на рынке: карточка, выставленная игроком на продажу.
 *
 * @param id        идентификатор лота, {@code 0} для ещё не сохранённого лота
 * @param sellerId  идентификатор продавца
 * @param heroId    идентификатор героя
 * @param price     цена лота в игровой валюте
 * @param createdAt дата и время создания лота
 */
public record MarketListing(
        long id,
        long sellerId,
        long heroId,
        int price,
        @NotNull LocalDateTime createdAt
) {

    /**
     * Создаёт лот с проверкой инвариантов.
     *
     * @throws IllegalArgumentException если идентификаторы не положительные,
     *                                  цена не положительная или время создания не задано
     */
    public MarketListing {
        if (id < 0) {
            throw new IllegalArgumentException("Listing id must be non-negative");
        }
        if (sellerId <= 0) {
            throw new IllegalArgumentException("Seller id must be positive");
        }
        if (heroId <= 0) {
            throw new IllegalArgumentException("Hero id must be positive");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    }
}
