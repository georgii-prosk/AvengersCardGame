package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PurchaseResult;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Сервис рынка карточек: выставление, покупка и снятие лотов.
 */
public interface MarketService {

    /**
     * Выставляет карточку пользователя на рынок.
     * <p>
     * Карточка резервируется: удаляется из коллекции продавца и сохраняется как лот.
     * Операция выполняется в одной транзакции.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена лота
     * @return созданный лот
     * @throws UserNotFoundException   если продавец не найден
     * @throws InvalidPriceException   если цена вне допустимых для редкости рамок
     * @throws NotEnoughCardsException если у продавца нет такой карточки
     */
    @NotNull MarketListing createListing(long sellerId, long heroId, int price)
            throws UserNotFoundException, InvalidPriceException, NotEnoughCardsException;

    /**
     * Покупает лот.
     * <p>
     * В одной транзакции лот удаляется, деньги списываются у покупателя,
     * продавцу начисляется сумма за вычетом комиссии, карточка передаётся покупателю.
     *
     * @param buyerId   идентификатор покупателя
     * @param listingId идентификатор лота
     * @return результат покупки
     * @throws UserNotFoundException     если покупатель или продавец не найден
     * @throws InsufficientFundsException если у покупателя недостаточно валюты
     * @throws ListingUnavailableException если лот уже недоступен или принадлежит покупателю
     */
    @NotNull PurchaseResult buy(long buyerId, long listingId)
            throws UserNotFoundException, InsufficientFundsException, ListingUnavailableException;

    /**
     * Снимает лот с рынка и возвращает карточку в коллекцию продавца.
     *
     * @param sellerId  идентификатор продавца
     * @param listingId идентификатор лота
     * @return снятый лот
     * @throws UserNotFoundException      если продавец не найден
     * @throws ListingUnavailableException если лот не найден или принадлежит другому игроку
     */
    @NotNull MarketListing cancelListing(long sellerId, long listingId)
            throws UserNotFoundException, ListingUnavailableException;

    /**
     * Возвращает все активные лоты для отображения на рынке.
     *
     * @return список лотов
     */
    @NotNull List<MarketItem> getActiveListings();

    /**
     * Возвращает активные лоты продавца.
     *
     * @param sellerId идентификатор продавца
     * @return список лотов продавца
     */
    @NotNull List<MarketItem> getUserListings(long sellerId);
}
