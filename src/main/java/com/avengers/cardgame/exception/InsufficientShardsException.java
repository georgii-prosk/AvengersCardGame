package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: у игрока недостаточно кусочков.
 */
public class InsufficientShardsException extends AppException {

    /** Требуемое количество кусочков. */
    private final int required;

    /** Доступное количество кусочков. */
    private final int available;

    /**
     * @param required  требуемое количество кусочков
     * @param available доступное количество кусочков
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

    /**
     * @return сколько кусочков не хватает
     */
    public int missing() {
        return Math.max(0, required - available);
    }
}
