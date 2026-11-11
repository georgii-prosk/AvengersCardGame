package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: недостаточно валюты для покупки.
 */
public class InsufficientFundsException extends AppException {

    /** Требуемая сумма. */
    private final int required;

    /** Доступная сумма. */
    private final int available;

    /**
     * @param required  требуемая сумма
     * @param available доступная сумма
     */
    public InsufficientFundsException(int required, int available) {
        super("Insufficient funds: required=" + required + ", available=" + available);
        this.required = required;
        this.available = available;
    }

    /**
     * @return требуемая сумма
     */
    public int required() {
        return required;
    }

    /**
     * @return доступная сумма
     */
    public int available() {
        return available;
    }
}
