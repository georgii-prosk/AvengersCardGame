package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.Hero;

import java.util.List;

/**
 * Сервис карточек пользователей.
 */
public interface CardService {

    /**
     * Выдаёт пользователю ежедневные карточки и запоминает время выдачи.
     *
     * @param userId идентификатор пользователя
     * @return список выпавших героев
     * @throws UserNotFoundException   если пользователь не найден
     * @throws DailyCooldownException  если с прошлой выдачи прошло меньше времени, чем кулдаун
     */
    List<Hero> grantDaily(long userId) throws UserNotFoundException, DailyCooldownException;
}
