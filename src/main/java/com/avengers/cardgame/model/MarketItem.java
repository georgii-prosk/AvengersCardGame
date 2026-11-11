package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Лот рынка, обогащённый данными героя и продавца для отображения.
 *
 * @param listing    данные лота
 * @param hero       выставленный герой
 * @param sellerName отображаемое имя продавца
 */
public record MarketItem(
        @NotNull MarketListing listing,
        @NotNull Hero hero,
        @NotNull String sellerName
) {

    /**
     * Создаёт элемент рынка с проверкой обязательных полей.
     */
    public MarketItem {
        Objects.requireNonNull(listing, "Listing must not be null");
        Objects.requireNonNull(hero, "Hero must not be null");
        Objects.requireNonNull(sellerName, "Seller name must not be null");
    }
}
