
package com.avengers.cardgame.repository;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для чтения справочника героев.
 */
public interface HeroRepository {

    /**
     * Находит всех героев.
     *
     * @return список героев
     */
    @NotNull List<Hero> findAll();

    /**
     * Находит героя по идентификатору.
     *
     * @param id идентификатор героя
     * @return герой, если найден
     */
    @NotNull Optional<Hero> findById(long id);

    /**
     * Находит героев по редкости.
     *
     * @param rarity редкость героя
     * @return список героев
     */
    @NotNull List<Hero> findByRarity(HeroRarity rarity);
}
