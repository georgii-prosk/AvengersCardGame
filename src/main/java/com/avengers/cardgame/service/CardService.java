package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

/**
 * Сервис для работы с карточками пользователей.
 */
public interface CardService {

    /**
     * Разбирает карточки игрока на кусочки.
     * <p>
     * Операция выполняется в одной транзакции: карточки удаляются,
     * кусочки начисляются, либо не происходит ничего.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @param count  количество карточек для разбора
     * @return результат разбора
     * @throws AppException при недостатке карточек или ошибке операции
     */
    DisassembleResult disassemble(long userId, long heroId, int count) throws AppException;

    /**
     * Крафтит случайную карточку выбранной редкости.
     * <p>
     * Операция выполняется в одной транзакции: кусочки списываются,
     * карточка добавляется в коллекцию, либо не происходит ничего.
     *
     * @param userId идентификатор пользователя
     * @param rarity выбранная редкость
     * @return результат крафта
     * @throws AppException при недостатке кусочков или ошибке операции
     */
    @NotNull CraftResult craft(long userId, @NotNull HeroRarity rarity) throws AppException;
}
