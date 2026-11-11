package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: у пользователя недостаточно карточек для операции.
 */
public class NotEnoughCardsException extends AppException {

    /** Идентификатор пользователя. */
    private final long userId;

    /** Идентификатор героя. */
    private final long heroId;

    /** Запрошенное количество карточек. */
    private final int requested;

    /** Доступное количество карточек. */
    private final int available;

    /**
     * @param userId    идентификатор пользователя
     * @param heroId    идентификатор героя
     * @param requested запрошенное количество
     * @param available доступное количество
     */
    public NotEnoughCardsException(long userId, long heroId, int requested, int available) {
        super("Not enough cards: userId=" + userId + ", heroId=" + heroId
                + ", requested=" + requested + ", available=" + available);
        this.userId = userId;
        this.heroId = heroId;
        this.requested = requested;
        this.available = available;
    }

    /**
     * @return идентификатор пользователя
     */
    public long userId() {
        return userId;
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
