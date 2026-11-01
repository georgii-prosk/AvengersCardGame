package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.HeroService;

import java.util.List;
import java.util.Optional;

/**
 * Сервис героев для тестов: по кругу возвращает заданных героев.
 */
public class FakeHeroService implements HeroService {

    private final List<Hero> heroes;
    private int next;

    public FakeHeroService(List<Hero> heroes) {
        this.heroes = heroes;
    }

    @Override
    public Optional<Hero> getRandomHero() {
        if (heroes.isEmpty()) {
            return Optional.empty();
        }
        Hero hero = heroes.get(next % heroes.size());
        next++;
        return Optional.of(hero);
    }
}