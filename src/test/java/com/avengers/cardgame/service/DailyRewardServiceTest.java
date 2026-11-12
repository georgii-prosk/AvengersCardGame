package com.avengers.cardgame.service;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeHeroService;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.fakes.ScriptedRandom;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.DailyRewardServiceImpl;
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
 * Тесты для {@link DailyRewardServiceImpl}.
 */
class DailyRewardServiceTest {

    /** Идентификатор тестового пользователя. */
    private static final long USER_ID = 42L;

    /** Кулдаун ежедневной награды. */
    private static final Duration COOLDOWN = Duration.ofHours(24);

    /** Фиксированный момент времени. */
    private static final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");

    /** Эпический герой. */
    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Редкий герой. */
    private final Hero widow = new Hero(7, "Black Widow", HeroRarity.RARE, 35, 25, 40, "r");

    /** Часы теста. */
    private final Clock clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);

    /** Текущее время теста. */
    private final LocalDateTime now = LocalDateTime.now(clock);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Сервис пользователей. */
    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        userService = new UserServiceImpl(userRepository);
    }

    /**
     * Создаёт сервис с указанными героями и сценарием случайности.
     *
     * @param heroes   герои для выдачи
     * @param scripted сценарий случайных чисел
     * @return сервис ежедневной награды
     */
    private DailyRewardService serviceWithHeroes(List<Hero> heroes, int... scripted) {
        return new DailyRewardServiceImpl(
                userService,
                userCardRepository,
                new FakeHeroService(heroes),
                new GameConfig(COOLDOWN, Duration.ofMinutes(30)),
                new ScriptedRandom(scripted),
                clock
        );
    }

    /**
     * @param scripted сценарий случайных чисел
     * @return сервис с эпическим и редким героями
     */
    private DailyRewardService serviceWith(int... scripted) {
        return serviceWithHeroes(List.of(ironMan, widow), scripted);
    }

    /**
     * Регистрирует пользователя.
     *
     * @param lastDailyAt время последней награды
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(LocalDateTime lastDailyAt) throws Exception {
        userRepository.save(User.of(
                USER_ID,
                PlayerName.of("Tony"),
                null,
                0,
                0,
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
        registerUser(null);

        DailyRewardResult result = serviceWith(1).grantDaily(USER_ID);

        DailyRewardResult.Success success =
                Assertions.assertInstanceOf(DailyRewardResult.Success.class, result);
        Assertions.assertEquals(List.of(ironMan, widow), success.heroes());
        Assertions.assertEquals(2, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт от одной до трёх карточек")
    void grantsBetweenOneAndThreeCards() throws Exception {
        registerUser(null);

        DailyRewardResult.Success first = Assertions.assertInstanceOf(DailyRewardResult.Success.class, serviceWith(0).grantDaily(USER_ID));
        Assertions.assertEquals(1, first.heroes().size());

        userRepository.updateLastDailyAt(USER_ID, now.minus(COOLDOWN));

        DailyRewardResult.Success second = Assertions.assertInstanceOf(DailyRewardResult.Success.class, serviceWith(2).grantDaily(USER_ID));
        Assertions.assertEquals(3, second.heroes().size());
    }

    @Test
    @DisplayName("После выдачи обновляет lastDailyAt")
    void updatesLastDailyAtAfterGrant() throws Exception {
        registerUser(null);

        serviceWith(0).grantDaily(USER_ID);

        Assertions.assertEquals(now, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("При активном кулдауне возвращает оставшееся время и ничего не выдаёт")
    void cooldownReturnsTimeLeftAndGivesNothing() throws Exception {
        LocalDateTime last = now.minusHours(23);
        registerUser(last);

        DailyRewardResult result = serviceWith(0).grantDaily(USER_ID);

        DailyRewardResult.Cooldown cooldown =
                Assertions.assertInstanceOf(DailyRewardResult.Cooldown.class, result);
        Assertions.assertEquals(Duration.ofHours(1), cooldown.timeUntilNextClaim());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(last, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("Повторный вызов сразу после выдачи возвращает кулдаун")
    void secondDailyRightAfterFirstIsRejected() throws Exception {
        registerUser(null);
        DailyRewardService service = serviceWith(0);

        DailyRewardResult first = service.grantDaily(USER_ID);
        DailyRewardResult second = service.grantDaily(USER_ID);

        Assertions.assertInstanceOf(DailyRewardResult.Success.class, first);
        DailyRewardResult.Cooldown secondCooldown = Assertions.assertInstanceOf(DailyRewardResult.Cooldown.class, second);
        Assertions.assertEquals(COOLDOWN, secondCooldown.timeUntilNextClaim());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт снова, когда кулдаун истёк ровно сейчас")
    void grantsAgainWhenCooldownExactlyElapsed() throws Exception {
        registerUser(now.minus(COOLDOWN));

        DailyRewardResult result = serviceWith(0).grantDaily(USER_ID);

        DailyRewardResult.Success success =
                Assertions.assertInstanceOf(DailyRewardResult.Success.class, result);
        Assertions.assertEquals(1, success.heroes().size());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException для неизвестного пользователя")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class, () -> serviceWith(0).grantDaily(USER_ID));
    }
}
