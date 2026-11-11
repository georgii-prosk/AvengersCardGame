package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Активный лот на рынке.
 *
 * @param id        идентификатор лота (0, если лот ещё не сохранён)
 * @param sellerId  идентификатор продавца
 * @param heroId    идентификатор выставленного героя
 * @param price     цена лота в основной валюте
 * @param createdAt дата и время выставления лота
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
     * @throws IllegalArgumentException если цена не положительная
     */
    public MarketListing {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
        Objects.requireNonNull(createdAt, "CreatedAt must not be null");
    }
}
