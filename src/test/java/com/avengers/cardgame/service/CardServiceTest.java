package com.avengers.cardgame.service;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.FakeHeroService;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.fakes.ScriptedRandom;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.CardServiceImpl;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Тесты для {@link CardServiceImpl}.
 */
class CardServiceTest {

    /** Идентификатор тестового пользователя. */
    private static final long USER_ID = 42L;

    /** Кулдаун ежедневной награды. */
    private final Duration COOLDOWN = Duration.ofHours(24);

    /** Фиксированный момент времени для тестов. */
    private final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");

    /** Эпический герой. */
    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Редкий герой. */
    private final Hero widow = new Hero(7, "Black Widow", HeroRarity.RARE, 35, 25, 40, "r");

    /** Обычный герой. */
    private final Hero commonHero = new Hero(1, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Фиксированные часы. */
    private final Clock clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);

    /** Текущий момент по фиксированным часам. */
    private final LocalDateTime now = LocalDateTime.now(clock);

    /** Игровая конфигурация. */
    private final GameConfig gameConfig = new GameConfig(COOLDOWN, Duration.ofMinutes(30));

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Репозиторий героев. */
    private InMemoryHeroRepository heroRepository;

    /** Сервис пользователей. */
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository(userRepository);
        heroRepository = new InMemoryHeroRepository();
        heroRepository.save(ironMan);
        heroRepository.save(widow);
        heroRepository.save(commonHero);
        userService = new UserServiceImpl(userRepository);
    }

    /**
     * Создаёт сервис для тестов ежедневной награды.
     *
     * @param scripted сценарий случайности
     * @return сервис карточек
     */
    private CardService dailyService(int... scripted) {
        return new CardServiceImpl(userService, userCardRepository,
                new FakeHeroService(List.of(ironMan, widow)), heroRepository, gameConfig,
                new ScriptedRandom(scripted), clock);
    }

    /**
     * Создаёт сервис для тестов разбора и крафта.
     *
     * @param heroes герои для выдачи
     * @return сервис карточек
     */
    private CardService economyService(List<Hero> heroes) {
        return new CardServiceImpl(userService, userCardRepository,
                new FakeHeroService(heroes), heroRepository, gameConfig,
                new ScriptedRandom(0), clock);
    }

    /**
     * Регистрирует пользователя с заданным балансом кусочков.
     *
     * @param shards баланс кусочков
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(int shards) throws Exception {
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                0, shards, 0, LocalDateTime.now(), LocalDateTime.now(), null, null));
    }

    /**
     * Регистрирует пользователя с заданным временем последней награды.
     *
     * @param lastDailyAt время последней выдачи /daily
     * @throws Exception при ошибке сохранения
     */
    private void registerDailyUser(LocalDateTime lastDailyAt) throws Exception {
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                0, 0, LocalDateTime.now(), LocalDateTime.now(), lastDailyAt, null));
    }

    @Test
    @DisplayName("Первая выдача возвращает героев и сохраняет их в коллекцию")
    void firstDailyGrantsCardsAndSavesThem() throws Exception {
        registerDailyUser(null);

        DailyRewardResult result = dailyService(1).grantDaily(USER_ID);

        Success success = Assertions.assertInstanceOf(Success.class, result);
        Assertions.assertEquals(List.of(ironMan, widow), success.heroes());
        Assertions.assertEquals(2, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт от одной до трёх карточек")
    void grantsBetweenOneAndThreeCards() throws Exception {
        registerDailyUser(null);

        Success first = Assertions.assertInstanceOf(Success.class, dailyService(0).grantDaily(USER_ID));
        Assertions.assertEquals(1, first.heroes().size());

        userRepository.updateLastDailyAt(USER_ID, now.minus(COOLDOWN));

        Success second = Assertions.assertInstanceOf(Success.class, dailyService(2).grantDaily(USER_ID));
        Assertions.assertEquals(3, second.heroes().size());
    }

    @Test
    @DisplayName("После выдачи обновляет lastDailyAt")
    void updatesLastDailyAtAfterGrant() throws Exception {
        registerDailyUser(null);

        dailyService(0).grantDaily(USER_ID);

        Assertions.assertEquals(now, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("При активном кулдауне возвращает оставшееся время и ничего не выдаёт")
    void cooldownReturnsTimeLeftAndGivesNothing() throws Exception {
        LocalDateTime last = now.minusHours(23);
        registerDailyUser(last);

        DailyRewardResult result = dailyService(0).grantDaily(USER_ID);

        Cooldown cooldown = Assertions.assertInstanceOf(Cooldown.class, result);
        Assertions.assertEquals(Duration.ofHours(1), cooldown.timeUntilNextClaim());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(last, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("Повторный вызов сразу после выдачи возвращает кулдаун")
    void secondDailyRightAfterFirstIsRejected() throws Exception {
        registerDailyUser(null);
        CardService service = dailyService(0);

        DailyRewardResult first = service.grantDaily(USER_ID);
        DailyRewardResult second = service.grantDaily(USER_ID);

        Assertions.assertInstanceOf(Success.class, first);
        Cooldown secondCooldown = Assertions.assertInstanceOf(Cooldown.class, second);
        Assertions.assertEquals(COOLDOWN, secondCooldown.timeUntilNextClaim());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт снова, когда кулдаун истёк ровно сейчас")
    void grantsAgainWhenCooldownExactlyElapsed() throws Exception {
        registerDailyUser(now.minus(COOLDOWN));

        DailyRewardResult result = dailyService(0).grantDaily(USER_ID);

        Success success = Assertions.assertInstanceOf(Success.class, result);
        Assertions.assertEquals(1, success.heroes().size());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException для неизвестного пользователя")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class, () -> dailyService(0).grantDaily(USER_ID));
    }

    @Test
    @DisplayName("Разбор карточек удаляет их и начисляет кусочки")
    void disassemblesCardsAndAddsShards() throws Exception {
        registerUser(5);
        userCardRepository.addCard(USER_ID, ironMan.id());
        userCardRepository.addCard(USER_ID, ironMan.id());
        userCardRepository.addCard(USER_ID, ironMan.id());
        int shardsYield = gameConfig.rarity(HeroRarity.EPIC).shardsYield();

        DisassembleResult result = economyService(List.of(ironMan)).disassemble(USER_ID, ironMan.id(), 2);

        Assertions.assertEquals(2, result.cardsDisassembled());
        Assertions.assertEquals(shardsYield * 2, result.shardsGained());
        Assertions.assertEquals(5 + shardsYield * 2, result.shardsBalance());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(5 + shardsYield * 2,
                userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("Разбор при нехватке карточек не меняет состояние")
    void disassembleRollsBackWhenNotEnoughCards() throws Exception {
        registerUser(5);
        userCardRepository.addCard(USER_ID, ironMan.id());

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> economyService(List.of(ironMan)).disassemble(USER_ID, ironMan.id(), 2));

        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(5, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("Крафт списывает кусочки и выдаёт карточку")
    void craftSpendsShardsAndAddsCard() throws Exception {
        registerUser(20);
        int craftCost = gameConfig.rarity(HeroRarity.COMMON).craftCost();

        CraftResult result = economyService(List.of(commonHero)).craft(USER_ID, HeroRarity.COMMON);

        Assertions.assertEquals(commonHero, result.hero());
        Assertions.assertEquals(craftCost, result.shardsSpent());
        Assertions.assertEquals(20 - craftCost, result.shardsBalance());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(20 - craftCost,
                userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("Крафт при нехватке кусочков не меняет состояние")
    void craftRollsBackWhenNotEnoughShards() throws Exception {
        registerUser(2);

        Assertions.assertThrows(InsufficientShardsException.class,
                () -> economyService(List.of(commonHero)).craft(USER_ID, HeroRarity.COMMON));

        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(2, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("Крафт при отсутствии героев нужной редкости не списывает кусочки")
    void craftRollsBackWhenRarityHasNoHeroes() throws Exception {
        registerUser(20);

        Assertions.assertThrows(IllegalStateException.class,
                () -> economyService(List.of(ironMan)).craft(USER_ID, HeroRarity.COMMON));

        Assertions.assertEquals(20, userRepository.findByUserId(USER_ID).orElseThrow().shards());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
    }
}