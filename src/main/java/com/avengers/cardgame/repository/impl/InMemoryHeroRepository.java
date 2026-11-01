package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.repository.HeroRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация {@link HeroRepository}, которая хранит справочник героев в памяти.
 */
public final class InMemoryHeroRepository implements HeroRepository {

    /** Хранилище героев: идентификатор героя → объект героя. */
    private final Map<Long, Hero> heroes;

    /**
     * Создаёт репозиторий и загружает всех героев из файла {@code heroes.json}.
     *
     * @throws IllegalStateException если файл не найден или произошла ошибка при его чтении
     */
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
    public @NotNull List<Hero> findAll() {
        return heroes.values().stream()
                .sorted(Comparator.comparingLong(Hero::id))
                .toList();
    }

    @Override
    public @NotNull Optional<Hero> findById(long id) {
        return Optional.ofNullable(heroes.get(id));
    }

    @Override
    public @NotNull List<Hero> findByRarity(HeroRarity rarity) {
        return findAll().stream()
                .filter(hero -> hero.rarity() == rarity)
                .toList();
    }
}