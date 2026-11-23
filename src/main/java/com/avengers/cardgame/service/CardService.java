package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.UserNotFoundException;
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
     * Разбирает карточки пользователя на кусочки.
     * <p>
     * Операция выполняется в одной транзакции: карточки списываются,
     * кусочки начисляются, либо не выполняется ничего.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @param count  количество разбираемых карточек
     * @return результат разбора
     * @throws AppException при ошибке разбора карточек или поиска пользователя
     */
    DisassembleResult disassemble(long userId, long heroId, int count) throws AppException;

    /**
     * Создаёт случайную карточку выбранной редкости за кусочки.
     * <p>
     * Операция выполняется в одной транзакции: кусочки списываются,
     * карточка добавляется, либо не выполняется ничего.
     *
     * @param userId идентификатор пользователя
     * @param rarity редкость карточки
     * @return результат крафта
     * @throws AppException при ошибке крафта или поиска пользователя
     */
    @NotNull CraftResult craft(long userId, @NotNull HeroRarity rarity) throws AppException;
}
