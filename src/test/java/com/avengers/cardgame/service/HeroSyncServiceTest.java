package com.avengers.cardgame.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link HeroSyncService}.
 */
class HeroSyncServiceTest {

    /** Фейковый репозиторий героев. */
    private InMemoryHeroRepository heroRepository;

    /** Тестируемый сервис. */
    private HeroSyncService syncService;

    /**
     * Создаёт фейковый репозиторий и сервис перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        heroRepository = new InMemoryHeroRepository();
        syncService = new HeroSyncService(heroRepository, new ObjectMapper());
    }

    /**
     * Проверяет, что герои из JSON загружаются в репозиторий.
     */
    @Test
    @DisplayName("Синхронизирует героев из JSON в репозиторий")
    void syncsHeroesFromJson() {
        syncService.sync();

        Assertions.assertTrue(heroRepository.size() > 0,
                "Репозиторий должен содержать героев после синхронизации");

        Assertions.assertTrue(heroRepository.findById(1L).isPresent());
        Assertions.assertEquals("Pepper Potts", heroRepository.findById(1L).get().name());
    }

    /**
     * Проверяет, что повторная синхронизация не создает дубликатов
     * (благодаря использованию Map в фейке).
     */
    @Test
    @DisplayName("Повторная синхронизация не создаёт дубликатов")
    void syncTwiceDoesNotDuplicate() {
        syncService.sync();
        int initialSize = heroRepository.size();

        syncService.sync();

        Assertions.assertEquals(initialSize, heroRepository.size());
    }
}