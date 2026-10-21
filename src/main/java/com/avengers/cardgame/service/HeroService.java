package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;

/**
 * Сервис героев: выбор случайной карточки с учётом редкости.
 */
public interface HeroService {

    /**
     * Возвращает случайного героя. Редкость определяется по весам
     * {@link com.avengers.cardgame.model.HeroRarity#weight()}, затем
     * внутри редкости герой выбирается равновероятно.
     *
     * @return случайный герой
     * @throws IllegalStateException если в справочнике нет ни одного героя
     */
    Hero getRandomHero();
}
