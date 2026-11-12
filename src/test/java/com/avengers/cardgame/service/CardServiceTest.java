package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.fakes.FakeConnectionFactory;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.fakes.FakeHeroService;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link CardServiceImpl}.
 */
class CardServiceTest {

    /** Идентификатор тестового пользователя. */
    private static final long USER_ID = 42L;

    /** Эпический герой. */
    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private FakeHeroRepository heroRepository;

    /** Фабрика фейковых соединений. */
    private FakeConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        heroRepository = new FakeHeroRepository();
        heroRepository.save(ironMan);
        heroRepository.save(commonHero);
        connectionFactory = new FakeConnectionFactory();
    }

    /**
     * Создаёт сервис карточек с указанными героями.
     *
     * @param heroes герои для выдачи
     * @return сервис карточек
     */
    private CardService serviceWithHeroes(List<Hero> heroes) {
        return new CardServiceImpl(
                userRepository,
                userCardRepository,
                heroRepository,
                new FakeHeroService(heroes),
                connectionFactory
        );
    }

    /**
     * @return сервис с эпическим героем
     */
    private CardService serviceWith() {
        return serviceWithHeroes(List.of(ironMan));
    }

    /**
     * Регистрирует пользователя с заданным балансом кусочков.
     *
     * @param shards баланс кусочков
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(int shards) throws Exception {
        userRepository.save(User.of(
                USER_ID,
                PlayerName.of("Tony"),
                null,
                0,
                shards,
                0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        ));
    }

    @Test
    @DisplayName("Разбор карточек удаляет их и начисляет кусочки")
    void disassemblesCardsAndAddsShards() throws Exception {
        registerUser(5);
        userCardRepository.addCard(USER_ID, ironMan.id());
        userCardRepository.addCard(USER_ID, ironMan.id());
        userCardRepository.addCard(USER_ID, ironMan.id());

        DisassembleResult result = serviceWith().disassemble(USER_ID, ironMan.id(), 2);

        Assertions.assertEquals(2, result.cardsDisassembled());
        Assertions.assertEquals(ironMan.rarity().shardsYield() * 2, result.shardsGained());
        Assertions.assertEquals(5 + ironMan.rarity().shardsYield() * 2, result.shardsBalance());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(1, connectionFactory.connection().commitCount());
        Assertions.assertEquals(0, connectionFactory.connection().rollbackCount());
    }

    @Test
    @DisplayName("Разбор при нехватке карточек откатывает транзакцию")
    void disassembleRollsBackWhenNotEnoughCards() throws Exception {
        registerUser(5);
        userCardRepository.addCard(USER_ID, ironMan.id());

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> serviceWith().disassemble(USER_ID, ironMan.id(), 2));

        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(5, userRepository.findByUserId(USER_ID).orElseThrow().shards());
        Assertions.assertEquals(0, connectionFactory.connection().commitCount());
        Assertions.assertEquals(1, connectionFactory.connection().rollbackCount());
    }

    @Test
    @DisplayName("Крафт списывает кусочки и выдаёт карточку")
    void craftSpendsShardsAndAddsCard() throws Exception {
        registerUser(20);

        CraftResult result = serviceWithHeroes(List.of(commonHero)).craft(USER_ID, HeroRarity.COMMON);

        Assertions.assertEquals(commonHero, result.hero());
        Assertions.assertEquals(HeroRarity.COMMON.craftCost(), result.shardsSpent());
        Assertions.assertEquals(20 - HeroRarity.COMMON.craftCost(), result.shardsBalance());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(20 - HeroRarity.COMMON.craftCost(),
                userRepository.findByUserId(USER_ID).orElseThrow().shards());
        Assertions.assertEquals(1, connectionFactory.connection().commitCount());
    }

    @Test
    @DisplayName("Крафт при нехватке кусочков откатывает транзакцию")
    void craftRollsBackWhenNotEnoughShards() throws Exception {
        registerUser(2);

        Assertions.assertThrows(InsufficientShardsException.class,
                () -> serviceWithHeroes(List.of(commonHero)).craft(USER_ID, HeroRarity.COMMON));

        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(2, userRepository.findByUserId(USER_ID).orElseThrow().shards());
        Assertions.assertEquals(0, connectionFactory.connection().commitCount());
        Assertions.assertEquals(1, connectionFactory.connection().rollbackCount());
    }

    @Test
    @DisplayName("Крафт при отсутствии героев нужной редкости откатывает транзакцию")
    void craftRollsBackWhenRarityHasNoHeroes() throws Exception {
        registerUser(20);

        Assertions.assertThrows(IllegalStateException.class,
                () -> serviceWithHeroes(List.of(ironMan)).craft(USER_ID, HeroRarity.COMMON));

        Assertions.assertEquals(20, userRepository.findByUserId(USER_ID).orElseThrow().shards());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(1, connectionFactory.connection().rollbackCount());
    }
}
