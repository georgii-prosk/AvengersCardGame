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

    /** Герои, которые возвращает фейк. */
    private final List<Hero> heroes;

    /** Индекс следующего героя. */
    private int next;

    /**
     * @param heroes герои, которые будет возвращать фейк
     */
    public FakeHeroService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
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

    @Override
    public Optional<Hero> getRandomHero(@NotNull HeroRarity rarity) {
        return heroes.stream()
                .filter(hero -> hero.rarity() == rarity)
                .findFirst();
    }
}
