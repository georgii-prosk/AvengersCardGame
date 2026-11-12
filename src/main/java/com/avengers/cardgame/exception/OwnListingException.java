package com.avengers.cardgame.exception;

/**
 * Доменная ошибка: попытка купить собственный лот на рынке.
 */
public class OwnListingException extends AppException {

    /** Идентификатор собственного лота. */
    private final long listingId;

    /**
     * @param listingId идентификатор лота
     */
    public OwnListingException(long listingId) {
        super("Own listing cannot be bought: listingId=" + listingId);
        this.listingId = listingId;
    }

    /**
     * @return идентификатор лота
     */
    public long listingId() {
        return listingId;
    }
}
