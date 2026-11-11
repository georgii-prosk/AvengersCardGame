package com.avengers.cardgame.service;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeConnection;
import com.avengers.cardgame.fakes.FakeConnectionFactory;
import com.avengers.cardgame.fakes.FakeHeroService;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
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

    private static final long USER_ID = 42L;
    private static final Duration COOLDOWN = Duration.ofHours(24);
    private static final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");

    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");
    private final Hero widow = new Hero(7, "Black Widow", HeroRarity.RARE, 35, 25, 40, "r");
    private final Clock clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);
    private final LocalDateTime now = LocalDateTime.now(clock);

    private InMemoryUserRepository userRepository;
    private InMemoryUserCardRepository userCardRepository;
    private InMemoryHeroRepository heroRepository;
    private FakeConnection connection;
    private FakeConnectionFactory connectionFactory;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        heroRepository = new InMemoryHeroRepository();
        heroRepository.save(ironMan);
        heroRepository.save(widow);
        connection = new FakeConnection();
        connectionFactory = new FakeConnectionFactory(connection);
        userService = new UserServiceImpl(userRepository);
    }

    /**
     * Создаёт сервис с заданным сценарием случайных чисел.
     *
     * @param scripted значения для последовательных вызовов random
     * @return сервис карточек
     */
    private CardService serviceWith(int... scripted) {
        return new CardServiceImpl(
                userService,
                userCardRepository,
                new FakeHeroService(List.of(ironMan, widow)),
                new GameConfig(COOLDOWN, Duration.ofMinutes(30), "Кристаллы", "💎"),
                new ScriptedRandom(scripted),
                clock,
                connectionFactory,
                heroRepository,
                userRepository
        );
    }

    /**
     * Сохраняет пользователя с заданными кусочками.
     *
     * @param shards      количество кусочков
     * @param lastDailyAt время последней награды
     * @throws Exception если данные пользователя некорректны
     */
    private void registerUser(int shards, LocalDateTime lastDailyAt) throws Exception {
        userRepository.save(User.of(
                USER_ID,
                PlayerName.of("Tony"),
                null,
                0,
                shards,
                0,
                LocalDateTime.now(),
                LocalDateTime.now(),
                lastDailyAt,
                null
        ));
    }

    @Test
    @DisplayName("Первая выдача возвращает героев и сохраняет их в коллекцию")
    void firstDailyGrantsCardsAndSavesThem() throws Exception {
        registerUser(0, null);

        DailyRewardResult result = serviceWith(1).grantDaily(USER_ID);

        Success success = Assertions.assertInstanceOf(Success.class, result);
        Assertions.assertEquals(List.of(ironMan, widow), success.heroes());
        Assertions.assertEquals(2, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт от одной до трёх карточек")
    void grantsBetweenOneAndThreeCards() throws Exception {
        registerUser(0, null);

        Success first = Assertions.assertInstanceOf(Success.class, serviceWith(0).grantDaily(USER_ID));
        Assertions.assertEquals(1, first.heroes().size());

        userRepository.updateLastDailyAt(USER_ID, now.minus(COOLDOWN));

        Success second = Assertions.assertInstanceOf(Success.class, serviceWith(2).grantDaily(USER_ID));
        Assertions.assertEquals(3, second.heroes().size());
    }

    @Test
    @DisplayName("После выдачи обновляет lastDailyAt")
    void updatesLastDailyAtAfterGrant() throws Exception {
        registerUser(0, null);

        serviceWith(0).grantDaily(USER_ID);

        Assertions.assertEquals(now, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("При активном кулдауне возвращает оставшееся время и ничего не выдаёт")
    void cooldownReturnsTimeLeftAndGivesNothing() throws Exception {
        LocalDateTime last = now.minusHours(23);
        registerUser(0, last);

        DailyRewardResult result = serviceWith(0).grantDaily(USER_ID);

        Cooldown cooldown = Assertions.assertInstanceOf(Cooldown.class, result);
        Assertions.assertEquals(Duration.ofHours(1), cooldown.timeUntilNextClaim());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(last, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("Повторный вызов сразу после выдачи возвращает кулдаун")
    void secondDailyRightAfterFirstIsRejected() throws Exception {
        registerUser(0, null);
        CardService service = serviceWith(0);

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
        registerUser(0, now.minus(COOLDOWN));

        DailyRewardResult result = serviceWith(0).grantDaily(USER_ID);

        Success success = Assertions.assertInstanceOf(Success.class, result);
        Assertions.assertEquals(1, success.heroes().size());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException для неизвестного пользователя")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class, () -> serviceWith(0).grantDaily(USER_ID));
    }

    @Test
    @DisplayName("Разбор карточек начисляет кусочки и фиксирует транзакцию")
    void disassembleAddsShardsAndCommits() throws Exception {
        registerUser(0, null);
        userCardRepository.addCard(USER_ID, ironMan.id());
        userCardRepository.addCard(USER_ID, ironMan.id());

        int shards = serviceWith().disassemble(USER_ID, ironMan.id(), 2);

        Assertions.assertEquals(ironMan.rarity().shardsYield() * 2, shards);
        Assertions.assertEquals(0, userCardRepository.countOf(USER_ID, ironMan.id()));
        Assertions.assertEquals(shards, userService.getByUserId(USER_ID).shards());
        Assertions.assertEquals(1, connection.commitCount());
        Assertions.assertEquals(0, connection.rollbackCount());
    }

    @Test
    @DisplayName("Разбор без карточек откатывает транзакцию")
    void disassembleWithoutCardsRollsBack() throws Exception {
        registerUser(0, null);

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> serviceWith().disassemble(USER_ID, ironMan.id(), 1));

        Assertions.assertEquals(1, connection.rollbackCount());
        Assertions.assertEquals(0, connection.commitCount());
        Assertions.assertEquals(0, userService.getByUserId(USER_ID).shards());
    }

    @Test
    @DisplayName("Разбор большего количества, чем есть, откатывает транзакцию")
    void disassembleTooManyCardsRollsBack() throws Exception {
        registerUser(0, null);
        userCardRepository.addCard(USER_ID, ironMan.id());

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> serviceWith().disassemble(USER_ID, ironMan.id(), 2));

        Assertions.assertEquals(1, connection.rollbackCount());
    }

    @Test
    @DisplayName("Крафт списывает кусочки и добавляет карточку")
    void craftSpendsShardsAndAddsCard() throws Exception {
        registerUser(HeroRarity.EPIC.craftCost() + 1, null);

        Hero hero = serviceWith().craft(USER_ID, HeroRarity.EPIC);

        Assertions.assertEquals(ironMan, hero);
        Assertions.assertEquals(1, userService.getByUserId(USER_ID).shards());
        Assertions.assertEquals(1, userCardRepository.countOf(USER_ID, ironMan.id()));
        Assertions.assertEquals(1, connection.commitCount());
    }

    @Test
    @DisplayName("Крафт без кусочков откатывает транзакцию")
    void craftWithoutShardsRollsBack() throws Exception {
        registerUser(0, null);

        Assertions.assertThrows(InsufficientShardsException.class,
                () -> serviceWith().craft(USER_ID, HeroRarity.LEGENDARY));

        Assertions.assertEquals(1, connection.rollbackCount());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(0, userService.getByUserId(USER_ID).shards());
    }
}
