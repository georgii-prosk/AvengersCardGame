package com.avengers.cardgame.repository;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.UserCard;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Репозиторий для работы с карточками пользователей.
 */
public interface UserCardRepository {

    /**
     * Добавляет карточку пользователю (или увеличивает количество).
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     */
    void addCard(long userId, long heroId);

    /**
     * Разбирает карточки пользователя на кусочки.
     * <p>
     * Операция атомарна: карточки списываются и кусочки начисляются в одной
     * транзакции либо не выполняются вообще.
     *
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @param count      количество разбираемых карточек
     * @param shardsGain количество начисляемых кусочков
     * @return баланс кусочков после операции
     * @throws com.avengers.cardgame.exception.NotEnoughCardsException если карточек недостаточно
     * @throws com.avengers.cardgame.exception.UserNotFoundException    если пользователь не найден
     */
    int disassemble(long userId, long heroId, int count, int shardsGain) throws AppException;

    /**
     * Создаёт карточку пользователю за кусочки.
     * <p>
     * Операция атомарна: кусочки списываются и карточка добавляется в одной
     * транзакции либо не выполняются вообще.
     *
     * @param userId     идентификатор пользователя
     * @param heroId     идентификатор героя
     * @param shardsCost стоимость крафта в кусочках
     * @return баланс кусочков после операции
     * @throws com.avengers.cardgame.exception.InsufficientShardsException если кусочков недостаточно
     * @throws com.avengers.cardgame.exception.UserNotFoundException       если пользователь не найден
     */
    int craft(long userId, long heroId, int shardsCost) throws AppException;

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    @NotNull List<UserCard> findByUserId(long userId);

    /**
     * Считает количество копий конкретной карточки у пользователя.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий карточки
     */
    int countByUserIdAndHeroId(long userId, long heroId);
}
