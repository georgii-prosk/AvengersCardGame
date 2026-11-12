package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: у игрока недостаточно копий карточки.
 */
public class NotEnoughCardsException extends AppException {

    /** Идентификатор героя. */
    private final long heroId;

    /** Запрошенное количество карточек. */
    private final int requested;

    /** Доступное количество карточек. */
    private final int available;

    /**
     * @param heroId    идентификатор героя
     * @param requested запрошенное количество
     * @param available доступное количество
     */
    public NotEnoughCardsException(long heroId, int requested, int available) {
        super("Not enough cards: heroId=" + heroId + ", requested=" + requested + ", available=" + available);
        this.heroId = heroId;
        this.requested = requested;
        this.available = available;
    }

    /**
     * @return идентификатор героя
     */
    public long heroId() {
        return heroId;
    }

    /**
     * @return запрошенное количество карточек
     */
    public int requested() {
        return requested;
    }

    /**
     * @return доступное количество карточек
     */
    public int available() {
        return available;
    }
}
