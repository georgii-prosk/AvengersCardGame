package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: лот уже продан, снят или не существует.
 */
public class ListingUnavailableException extends AppException {

    /** Идентификатор недоступного лота. */
    private final long listingId;

    /**
     * @param listingId идентификатор недоступного лота
     */
    public ListingUnavailableException(long listingId) {
        super("Listing is unavailable: listingId=" + listingId);
        this.listingId = listingId;
    }

    /**
     * @return идентификатор недоступного лота
     */
    public long listingId() {
        return listingId;
    }
}
