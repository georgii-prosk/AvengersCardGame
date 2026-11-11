package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис для работы с карточками пользователей.
 */
public interface CardService {

    /**
     * Выдаёт ежедневную награду пользователю.
     * <p>
     * Если с прошлой выдачи прошло достаточно времени, генерирует карточки
     * и обновляет время последней выдачи. В противном случае возвращает
     * информацию о том, сколько осталось ждать.
     *
     * @param userId идентификатор пользователя
     * @return результат выдачи (список героев или оставшееся время кулдауна)
     * @throws UserNotFoundException если пользователь с таким идентификатором не найден
     */
    DailyRewardResult grantDaily(long userId) throws UserNotFoundException;

    /**
     * Разбирает карточки героя на кусочки.
     * <p>
     * Операция выполняется в одной транзакции: либо списываются все карточки
     * и начисляются кусочки, либо не меняется ничего.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @param count  количество разбираемых карточек
     * @return количество полученных кусочков
     * @throws UserNotFoundException    если пользователь не найден
     * @throws NotEnoughCardsException  если у пользователя недостаточно карточек
     * @throws IllegalArgumentException если количество не положительное или герой не найден
     */
    int disassemble(long userId, long heroId, int count)
            throws UserNotFoundException, NotEnoughCardsException;

    /**
     * Крафтит случайного героя заданной редкости.
     * <p>
     * Операция выполняется в одной транзакции: либо списываются кусочки
     * и выдаётся карточка, либо не меняется ничего.
     *
     * @param userId идентификатор пользователя
     * @param rarity требуемая редкость героя
     * @return выданный герой
     * @throws UserNotFoundException      если пользователь не найден
     * @throws InsufficientShardsException если кусочков недостаточно
     */
    Hero craft(long userId, @NotNull HeroRarity rarity)
            throws UserNotFoundException, InsufficientShardsException;
}
