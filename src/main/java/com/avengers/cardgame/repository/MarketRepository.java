package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.MarketListing;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с лотами рынка.
 */
public interface MarketRepository {

    /**
     * Выставляет лот на рынок.
     * <p>
     * Операция атомарна: карточка снимается у продавца и лот сохраняется
     * в одной транзакции либо не выполняется вообще.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена лота
     * @return сохранённый лот с присвоенным идентификатором
     * @throws com.avengers.cardgame.exception.NotEnoughCardsException если у продавца нет карточки
     */
    @NotNull MarketListing createListing(long sellerId, long heroId, int price) throws AppException;

    /**
     * Покупает лот.
     * <p>
     * Операция атомарна: лот удаляется, цена списывается с покупателя,
     * продавцу начисляется переданная сумма, покупателю добавляется карточка.
     * <p>
     * Цена лота неизменяема, поэтому сумма продавца, рассчитанная по заранее
     * прочитанному лоту, совпадает с ценой удалённого лота.
     *
     * @param buyerId      идентификатор покупателя
     * @param listingId    идентификатор лота
     * @param sellerAmount сумма, которую получит продавец
     * @return удалённый лот
     * @throws com.avengers.cardgame.exception.ListingUnavailableException если лот уже недоступен
     * @throws com.avengers.cardgame.exception.InsufficientFundsException  если у покупателя не хватает средств
     * @throws com.avengers.cardgame.exception.UserNotFoundException       если покупатель или продавец не найден
     */
    @NotNull MarketListing buy(long buyerId, long listingId, int sellerAmount) throws AppException;

    /**
     * Снимает лот с продажи.
     * <p>
     * Операция атомарна: лот удаляется и карточка возвращается продавцу
     * в одной транзакции либо не выполняется вообще.
     *
     * @param sellerId  идентификатор продавца
     * @param listingId идентификатор лота
     * @return снятый лот
     * @throws com.avengers.cardgame.exception.ListingUnavailableException если лот уже недоступен или принадлежит другому игроку
     */
    @NotNull MarketListing cancelListing(long sellerId, long listingId) throws AppException;

    /**
     * Находит лот по идентификатору.
     *
     * @param listingId идентификатор лота
     * @return лот, если найден
     */
    @NotNull Optional<MarketListing> findById(long listingId);

    /**
     * Возвращает все активные лоты.
     *
     * @return список лотов
     */
    @NotNull List<MarketListing> findAllActive();

    /**
     * Возвращает все активные лоты продавца.
     *
     * @param sellerId идентификатор продавца
     * @return список лотов
     */
    @NotNull List<MarketListing> findBySeller(long sellerId);
}
