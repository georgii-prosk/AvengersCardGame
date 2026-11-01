package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;

import java.util.Optional;

/**
 * Сервис героев: выбор случайной карточки с учётом редкости.
 */
public interface HeroService {

    /**
     * Возвращает случайного героя. Редкость определяется по весам,
     * затем внутри редкости герой выбирается равновероятно.
     *
     * @return случайный герой, либо пустой {@link Optional}, если справочник пуст
     */
    Optional<Hero> getRandomHero();
}