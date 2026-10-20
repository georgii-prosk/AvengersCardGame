package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Фейковый репозиторий героев в памяти.
 * <p>
 * Полноценная реализация {@link HeroRepository} без обращения к БД.
 * Используется в тестах.
 */
public class InMemoryHeroRepository implements HeroRepository {

    /** Хранилище героев по идентификатору. */
    private final Map<Long, Hero> storage = new HashMap<>();

    @Override
    public List<Hero> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public Optional<Hero> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Hero> findByRarity(HeroRarity rarity) {
        return storage.values().stream()
                .filter(hero -> hero.rarity() == rarity)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Hero hero) {
        storage.put(hero.id(), hero);
    }

    /**
     * Возвращает количество сохранённых героев.
     *
     * @return количество героев
     */
    public int size() {
        return storage.size();
    }
}