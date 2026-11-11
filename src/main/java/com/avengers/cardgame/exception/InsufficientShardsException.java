package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: недостаточно кусочков для крафта.
 */
public class InsufficientShardsException extends AppException {

    /** Требуемое количество кусочков. */
    private final int required;

    /** Доступное количество кусочков. */
    private final int available;

    /**
     * @param required  требуемое количество
     * @param available доступное количество
     */
    public InsufficientShardsException(int required, int available) {
        super("Insufficient shards: required=" + required + ", available=" + available);
        this.required = required;
        this.available = available;
    }

    /**
     * @return требуемое количество кусочков
     */
    public int required() {
        return required;
    }

    /**
     * @return доступное количество кусочков
     */
    public int available() {
        return available;
    }
}
