package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InMemoryHeroRepository implements HeroRepository {

    private final Map<Long, Hero> heroes;

    public InMemoryHeroRepository() {
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("heroes.json")) {
            if (input == null) {
                throw new IllegalStateException("heroes.json not found");
            }

            List<Hero> loadedHeroes = new ObjectMapper()
                    .readValue(input, new TypeReference<>() {});

            this.heroes = loadedHeroes.stream()
                    .collect(Collectors.toUnmodifiableMap(
                            Hero::id,
                            Function.identity()
                    ));
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load heroes from heroes.json", exception);
        }
    }

    @Override
    public List<Hero> findAll() {
        return heroes.values().stream()
                .sorted(Comparator.comparingLong(Hero::id))
                .toList();
    }

    @Override
    public Optional<Hero> findById(long id) {
        return Optional.ofNullable(heroes.get(id));
    }

    @Override
    public List<Hero> findByRarity(HeroRarity rarity) {
        return findAll().stream()
                .filter(hero -> hero.rarity() == rarity)
                .toList();
    }
}