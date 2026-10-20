package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с героями.
 */
public interface HeroRepository {

    /**
     * Находит всех героев.
     *
     * @return список героев
     */
    List<Hero> findAll();

    /**
     * Находит героя по идентификатору.
     *
     * @param id идентификатор
     * @return герой, если найден
     */
    Optional<Hero> findById(long id);

    /**
     * Находит героев по редкости.
     *
     * @param rarity редкость
     * @return список героев
     */
    List<Hero> findByRarity(HeroRarity rarity);

    /**
     * Сохраняет или обновляет героя.
     *
     * @param hero герой для сохранения
     */
    void save(Hero hero);
}