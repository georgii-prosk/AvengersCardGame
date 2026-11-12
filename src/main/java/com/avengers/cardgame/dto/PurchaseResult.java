package com.avengers.cardgame.dto;

/**
 * Результат успешной покупки лота.
 *
 * @param buyerId  идентификатор покупателя
 * @param sellerId идентификатор продавца
 * @param heroId   идентификатор приобретённого героя
 * @param price    уплаченная цена
 */
public record PurchaseResult(long buyerId, long sellerId, long heroId, int price) {

    /**
     * Создаёт результат покупки с проверкой инвариантов.
     *
     * @throws IllegalArgumentException если цена не положительная
     */
    public PurchaseResult {
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }
}
