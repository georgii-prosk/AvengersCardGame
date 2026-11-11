package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

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

    /**
     * Возвращает случайного героя указанной редкости без фоллбэка на другие редкости.
     *
     * @param rarity требуемая редкость
     * @return случайный герой нужной редкости, либо пустой {@link Optional}, если таких героев нет
     */
    Optional<Hero> getRandomHero(@NotNull HeroRarity rarity);
}
