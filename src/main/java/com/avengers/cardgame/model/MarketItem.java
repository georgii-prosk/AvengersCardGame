package com.avengers.cardgame.model;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Готовое для отображения объявление рынка.
 *
 * @param listing    лот
 * @param hero       герой, выставленный на продажу
 * @param sellerName отображаемое имя продавца
 */
public record MarketItem(
        @NotNull MarketListing listing,
        @NotNull Hero hero,
        @NotNull String sellerName
) {

    /**
     * Создаёт элемент рынка с проверкой инвариантов.
     *
     * @throws IllegalArgumentException если имя продавца пустое
     */
    public MarketItem {
        Objects.requireNonNull(listing, "Listing must not be null");
        Objects.requireNonNull(hero, "Hero must not be null");
        Objects.requireNonNull(sellerName, "Seller name must not be null");
        if (sellerName.isBlank()) {
            throw new IllegalArgumentException("Seller name must not be blank");
        }
    }
}
