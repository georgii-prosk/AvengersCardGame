package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.HeroService;

import java.util.List;

/**
 * Сервис героев для тестов: по кругу возвращает заданных героев.
 */
public class FakeHeroService implements HeroService {

    /** Герои, выдаваемые по кругу. */
    private final List<Hero> heroes;

    /** Индекс следующего героя. */
    private int next;

    /**
     * @param heroes герои, выдаваемые по кругу
     */
    public FakeHeroService(List<Hero> heroes) {
        this.heroes = heroes;
    }

    @Override
    public Hero getRandomHero() {
        Hero hero = heroes.get(next % heroes.size());
        next++;
        return hero;
    }
}
