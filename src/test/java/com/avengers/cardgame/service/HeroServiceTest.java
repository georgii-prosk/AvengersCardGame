package com.avengers.cardgame.service;

import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.ScriptedRandom;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.service.impl.HeroServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Random;

/**
 * Тесты для {@link HeroServiceImpl}.
 */
class HeroServiceTest {

    /** Обычный герой №1. */
    private final Hero common1 = new Hero(1, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c1");

    /** Обычный герой №2. */
    private final Hero common2 = new Hero(2, "Mobius", HeroRarity.COMMON, 10, 25, 15, "c2");

    /** Редкий герой. */
    private final Hero rare = new Hero(3, "Black Widow", HeroRarity.RARE, 35, 25, 40, "r");

    /** Эпический герой. */
    private final Hero epic = new Hero(4, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Легендарный герой. */
    private final Hero legendary = new Hero(5, "Loki", HeroRarity.LEGENDARY, 200, 250, 200, "l");

    /**
     * Создаёт репозиторий с указанными героями.
     *
     * @param heroes герои
     * @return наполненный репозиторий
     */
    private InMemoryHeroRepository repositoryOf(Hero... heroes) {
        InMemoryHeroRepository repository = new InMemoryHeroRepository();
        for (Hero hero : heroes) {
            repository.save(hero);
        }
        return repository;
    }

    /**
     * @return репозиторий со всеми пятью героями
     */
    private InMemoryHeroRepository fullRepository() {
        return repositoryOf(common1, common2, rare, epic, legendary);
    }

    /**
     * Проверяет, что бросок даёт героя ожидаемой редкости.
     *
     * @param roll     бросок редкости (0–99)
     * @param expected ожидаемая редкость
     */
    private void assertRollGivesRarity(int roll, HeroRarity expected) {
        HeroService service = new HeroServiceImpl(fullRepository(), new ScriptedRandom(roll, 0));

        Assertions.assertEquals(expected, service.getRandomHero().rarity(), "roll=" + roll);
    }

    @Test
    @DisplayName("Броски 0–59 дают обычного героя")
    void rollsFrom0To59GiveCommon() {
        assertRollGivesRarity(0, HeroRarity.COMMON);
        assertRollGivesRarity(59, HeroRarity.COMMON);
    }

    @Test
    @DisplayName("Броски 60–84 дают редкого героя")
    void rollsFrom60To84GiveRare() {
        assertRollGivesRarity(60, HeroRarity.RARE);
        assertRollGivesRarity(84, HeroRarity.RARE);
    }

    @Test
    @DisplayName("Броски 85–96 дают эпического героя")
    void rollsFrom85To96GiveEpic() {
        assertRollGivesRarity(85, HeroRarity.EPIC);
        assertRollGivesRarity(96, HeroRarity.EPIC);
    }

    @Test
    @DisplayName("Броски 97–99 дают легендарного героя")
    void rollsFrom97To99GiveLegendary() {
        assertRollGivesRarity(97, HeroRarity.LEGENDARY);
        assertRollGivesRarity(99, HeroRarity.LEGENDARY);
    }

    @Test
    @DisplayName("Выбирает героя внутри редкости по случайному индексу")
    void picksHeroInsideRarityByRandomIndex() {
        HeroService first = new HeroServiceImpl(fullRepository(), new ScriptedRandom(10, 0));
        HeroService second = new HeroServiceImpl(fullRepository(), new ScriptedRandom(10, 1));

        Assertions.assertEquals(common1, first.getRandomHero());
        Assertions.assertEquals(common2, second.getRandomHero());
    }

    @Test
    @DisplayName("Если героев нужной редкости нет, берёт любого другого")
    void fallsBackToAnyHeroWhenRarityHasNoHeroes() {
        HeroService service = new HeroServiceImpl(repositoryOf(common1), new ScriptedRandom(99, 0));

        Assertions.assertEquals(common1, service.getRandomHero());
    }

    @Test
    @DisplayName("Бросает IllegalStateException, если героев нет вообще")
    void throwsWhenNoHeroesAtAll() {
        HeroService service = new HeroServiceImpl(repositoryOf(), new ScriptedRandom(0));

        Assertions.assertThrows(IllegalStateException.class, service::getRandomHero);
    }

    @Test
    @DisplayName("Не падает на множестве вызовов и возвращает известных героев")
    void neverFailsAndReturnsKnownHeroesOnManyCalls() {
        InMemoryHeroRepository repository = fullRepository();
        HeroService service = new HeroServiceImpl(repository, new Random(42));

        for (int i = 0; i < 1000; i++) {
            Assertions.assertTrue(repository.findAll().contains(service.getRandomHero()));
        }
    }
}
