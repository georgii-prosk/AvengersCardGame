package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.HeroService;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

/**
 * Сервис героев для тестов: по кругу возвращает заданных героев.
 */
public class FakeHeroService implements HeroService {

    /** Доступные герои. */
    private final List<Hero> heroes;

    /** Индекс следующего героя. */
    private int next;

    /**
     * @param heroes доступные герои
     */
    public FakeHeroService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Hero> getRandomHero() {
        return pick(heroes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Hero> getRandomHero(@NotNull HeroRarity rarity) {
        return pick(heroes.stream().filter(hero -> hero.rarity() == rarity).toList());
    }

    /**
     * Возвращает следующего героя из списка по кругу.
     *
     * @param candidates список кандидатов
     * @return герой, если список не пуст
     */
    private Optional<Hero> pick(List<Hero> candidates) {
        if (candidates.isEmpty()) {
            return Optional.empty();
        }
        Hero hero = candidates.get(next % candidates.size());
        next++;
        return Optional.of(hero);
    }
}
