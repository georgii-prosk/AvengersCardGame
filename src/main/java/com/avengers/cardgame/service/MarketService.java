package com.avengers.cardgame.service;

import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Сервис рынка: выставление, покупка и снятие лотов.
 */
public interface MarketService {

    /** Комиссия рынка при продаже в процентах. */
    int COMMISSION_PERCENT = 5;

    /**
     * Выставляет карточку игрока на продажу.
     * <p>
     * Карточка резервируется и удаляется из коллекции продавца.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена лота
     * @return созданный лот
     * @throws AppException если карточки нет или цена недопустима
     */
    @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException;

    /**
     * Покупает лот.
     * <p>
     * Покупатель платит цену лота, продавец получает сумму за вычетом комиссии,
     * карточка переходит покупателю. Все действия выполняются в одной транзакции.
     *
     * @param buyerId   идентификатор покупателя
     * @param listingId идентификатор лота
     * @return результат покупки
     * @throws AppException если лот недоступен или не хватает средств
     */
    @NotNull PurchaseResult buy(long buyerId, long listingId) throws AppException;

    /**
     * Снимает лот с продажи и возвращает карточку в коллекцию.
     *
     * @param sellerId  идентификатор продавца
     * @param listingId идентификатор лота
     * @return снятый лот
     * @throws AppException если лот недоступен
     */
    @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException;

    /**
     * Возвращает готовое для отображения объявление по идентификатору лота.
     *
     * @param listingId идентификатор лота
     * @return объявление рынка
     * @throws ListingUnavailableException если лот недоступен
     */
    @NotNull MarketItem getListing(long listingId) throws ListingUnavailableException;

    /**
     * Возвращает все активные лоты.
     *
     * @return список объявлений
     */
    @NotNull List<MarketItem> getActiveListings();

    /**
     * Возвращает активные лоты указанной редкости.
     *
     * @param rarity редкость или {@code null} для всех редкостей
     * @return список объявлений
     */
    @NotNull List<MarketItem> getActiveListings(@Nullable HeroRarity rarity);

    /**
     * Возвращает лоты игрока.
     *
     * @param sellerId идентификатор продавца
     * @return список объявлений
     */
    @NotNull List<MarketItem> getUserListings(long sellerId);
}
