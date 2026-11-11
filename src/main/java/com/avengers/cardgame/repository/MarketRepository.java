package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.MarketListing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с лотами рынка.
 */
public interface MarketRepository {

    /**
     * Сохраняет новый лот в рамках заданного соединения.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param listing    лот для сохранения
     * @return сохранённый лот с присвоенным идентификатором
     */
    @NotNull MarketListing saveListing(@Nullable Connection connection, @NotNull MarketListing listing);

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

    /**
     * Удаляет лот и возвращает его данные.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param listingId  идентификатор лота
     * @return удалённый лот, либо пустой {@link Optional}, если лот уже отсутствует
     */
    @NotNull Optional<MarketListing> deleteListingAndReturn(@Nullable Connection connection, long listingId);

    /**
     * Удаляет лот продавца и возвращает его данные.
     *
     * @param connection соединение с БД, может быть {@code null}
     * @param listingId  идентификатор лота
     * @param sellerId   идентификатор продавца
     * @return удалённый лот, либо пустой {@link Optional}, если лот уже отсутствует или принадлежит другому игроку
     */
    @NotNull Optional<MarketListing> deleteListingBySellerAndReturn(
            @Nullable Connection connection, long listingId, long sellerId);
}
