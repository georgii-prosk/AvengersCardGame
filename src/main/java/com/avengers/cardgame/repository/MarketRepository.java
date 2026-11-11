package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.MarketListing;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий активных лотов рынка.
 */
public interface MarketRepository {

    /**
     * Сохраняет новый лот и возвращает его с присвоенным идентификатором.
     *
     * @param connection соединение с активной транзакцией
     * @param listing    лот без идентификатора
     * @return сохранённый лот
     */
    @NotNull MarketListing saveListing(@NotNull Connection connection, @NotNull MarketListing listing);

    /**
     * Находит лот по идентификатору.
     *
     * @param connection соединение с активной транзакцией
     * @param listingId  идентификатор лота
     * @return лот, если он существует
     */
    @SuppressWarnings("unused")
    @NotNull Optional<MarketListing> findById(@NotNull Connection connection, long listingId);

    /**
     * Возвращает все активные лоты.
     *
     * @return список лотов, сначала новые
     */
    @NotNull List<MarketListing> findAllActive();

    /**
     * Возвращает активные лоты продавца.
     *
     * @param sellerId идентификатор продавца
     * @return список лотов продавца
     */
    @NotNull List<MarketListing> findBySeller(long sellerId);

    /**
     * Атомарно удаляет лот и возвращает его данные.
     *
     * @param connection соединение с активной транзакцией
     * @param listingId  идентификатор лота
     * @return удалённый лот, если он существовал
     */
    @NotNull Optional<MarketListing> deleteListingAndReturn(@NotNull Connection connection, long listingId);

    /**
     * Атомарно удаляет лот продавца и возвращает его данные.
     *
     * @param connection соединение с активной транзакцией
     * @param listingId  идентификатор лота
     * @param sellerId   идентификатор продавца
     * @return удалённый лот, если он принадлежал продавцу
     */
    @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @NotNull Connection connection, long listingId, long sellerId);
}
