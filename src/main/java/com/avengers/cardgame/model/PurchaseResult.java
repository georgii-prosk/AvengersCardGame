package com.avengers.cardgame.model;

/**
 * Результат покупки лота на рынке.
 *
 * @param buyerId  идентификатор покупателя
 * @param sellerId идентификатор продавца
 * @param heroId   идентификатор переданного героя
 * @param price    цена сделки в основной валюте
 */
public record PurchaseResult(long buyerId, long sellerId, long heroId, int price) {

    /**
     * Создаёт результат покупки с проверкой цены.
     *
     * @throws IllegalArgumentException если цена не положительная
     */
    public PurchaseResult {
        if (price <= 0) throw new IllegalArgumentException("Price must be positive");
    }
}
