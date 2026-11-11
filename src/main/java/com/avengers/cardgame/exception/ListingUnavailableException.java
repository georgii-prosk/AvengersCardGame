package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: лот больше недоступен (продан, снят или принадлежит покупателю).
 */
public class ListingUnavailableException extends AppException {

    /** Идентификатор лота. */
    private final long listingId;

    /** Причина недоступности лота. */
    private final String reason;

    /**
     * @param listingId идентификатор лота
     */
    public ListingUnavailableException(long listingId) {
        this(listingId, "listing not found");
    }

    /**
     * @param listingId идентификатор лота
     * @param reason    причина недоступности
     */
    public ListingUnavailableException(long listingId, String reason) {
        super("Listing is unavailable: id=" + listingId + ", reason=" + reason);
        this.listingId = listingId;
        this.reason = reason;
    }

    /**
     * @return идентификатор лота
     */
    public long listingId() {
        return listingId;
    }

    /**
     * @return причина недоступности лота
     */
    public String reason() {
        return reason;
    }
}
