package com.avengers.cardgame.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.repository.HeroRepository;

import java.io.InputStream;
import java.util.List;

/**
 * Сервис для синхронизации справочника героев из JSON в БД.
 */
public class HeroSyncService {

    /** Репозиторий героев. */
    private final HeroRepository heroRepository;

    /** Mapper для чтения JSON. */
    private final ObjectMapper objectMapper;

    /**
     * @param heroRepository репозиторий героев
     * @param objectMapper   mapper для JSON
     */
    public HeroSyncService(HeroRepository heroRepository, ObjectMapper objectMapper) {
        this.heroRepository = heroRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Читает JSON и сохраняет (UPSERT) героев в БД.
     *
     * @throws RuntimeException если файл не найден или произошла ошибка чтения
     */
    public void sync() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("heroes.json")) {
            if (is == null) {
                throw new IllegalStateException("File heroes.json not found in classpath");
            }
            List<Hero> heroes = objectMapper.readValue(is, new TypeReference<>() {});
            for (Hero hero : heroes) {
                heroRepository.save(hero);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync heroes from JSON", e);
        }
    }
}