package com.avengers.cardgame.service;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.DailyCooldownException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeHeroService;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.fakes.ScriptedRandom;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;                 // <-- добавить
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

    /** Кулдаун в тестах. */
    private static final Duration COOLDOWN = Duration.ofHours(24);

    /** Зафиксированный момент «сейчас». */
    private static final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");

    /** Герой для выдачи. */
    private final Hero ironMan = new Hero(9, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Другой герой для выдачи. */
    private final Hero widow = new Hero(7, "Black Widow", HeroRarity.RARE, 35, 25, 40, "r");

    /** Часы, остановленные на {@link #NOW_INSTANT}. */
    private final Clock clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);

    /** «Сейчас» в виде локального времени. */
    private final LocalDateTime now = LocalDateTime.now(clock);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Сервис пользователей. */
    private UserService userService;

    /**
     * Создаёт чистые репозитории перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        userService = new UserServiceImpl(userRepository);
    }

    /**
     * Создаёт сервис, у которого {@code random.nextInt(3)} возвращает заданные значения.
     *
     * @param scripted значения: 0 — одна карточка, 1 — две, 2 — три
     * @return сервис карточек
     */
    private CardService serviceWith(int... scripted) {
        return new CardServiceImpl(
                userService,
                userCardRepository,
                new FakeHeroService(List.of(ironMan, widow)),
                new GameConfig(COOLDOWN, Duration.ofMinutes(30)),
                new ScriptedRandom(scripted),
                clock
        );
    }

    /**
     * Регистрирует пользователя с заданным временем последней выдачи.
     *
     * @param lastDailyAt время последней выдачи или {@code null}
     * @throws Exception если сохранить пользователя не удалось
     */
    private void registerUser(LocalDateTime lastDailyAt) throws Exception {
        userRepository.save(User.of(
                USER_ID,
                PlayerName.of("Tony"),
                null,           // bio
                0,              // balance
                0,              // cardCount
                null,           // createdAt
                null,           // updatedAt
                lastDailyAt,    // lastDailyAt
                null            // dailyNotifiedAt
        ));
    }

    @Test
    @DisplayName("Первая выдача возвращает героев и сохраняет их в коллекцию")
    void firstDailyGrantsCardsAndSavesThem() throws Exception {
        registerUser(null);

        List<Hero> granted = serviceWith(1).grantDaily(USER_ID);

        Assertions.assertEquals(List.of(ironMan, widow), granted);
        Assertions.assertEquals(2, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт от одной до трёх карточек")
    void grantsBetweenOneAndThreeCards() throws Exception {
        registerUser(null);
        Assertions.assertEquals(1, serviceWith(0).grantDaily(USER_ID).size());

        userRepository.updateLastDailyAt(USER_ID, now.minus(COOLDOWN));
        Assertions.assertEquals(3, serviceWith(2).grantDaily(USER_ID).size());
    }

    @Test
    @DisplayName("После выдачи обновляет lastDailyAt")
    void updatesLastDailyAtAfterGrant() throws Exception {
        registerUser(null);

        serviceWith(0).grantDaily(USER_ID);

        Assertions.assertEquals(now, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("При активном кулдауне бросает исключение с оставшимся временем и ничего не выдаёт")
    void throwsCooldownWithTimeLeftAndGivesNothing() throws Exception {
        LocalDateTime last = now.minusHours(23);
        registerUser(last);

        DailyCooldownException e = Assertions.assertThrows(DailyCooldownException.class,
                () -> serviceWith(0).grantDaily(USER_ID));

        Assertions.assertEquals(Duration.ofHours(1), e.timeLeft());
        Assertions.assertEquals(0, userCardRepository.countByUserId(USER_ID));
        Assertions.assertEquals(last, userService.getByUserId(USER_ID).lastDailyAt());
    }

    @Test
    @DisplayName("Повторный вызов сразу после выдачи отклоняется кулдауном")
    void secondDailyRightAfterFirstIsRejected() throws Exception {
        registerUser(null);
        CardService service = serviceWith(0);

        service.grantDaily(USER_ID);
        DailyCooldownException e = Assertions.assertThrows(DailyCooldownException.class,
                () -> service.grantDaily(USER_ID));

        Assertions.assertEquals(COOLDOWN, e.timeLeft());
        Assertions.assertEquals(1, userCardRepository.countByUserId(USER_ID));
    }

    @Test
    @DisplayName("Выдаёт снова, когда кулдаун истёк ровно сейчас")
    void grantsAgainWhenCooldownExactlyElapsed() throws Exception {
        registerUser(now.minus(COOLDOWN));

        List<Hero> granted = serviceWith(0).grantDaily(USER_ID);

        Assertions.assertEquals(1, granted.size());
    }

    @Test
    @DisplayName("Бросает UserNotFoundException для неизвестного пользователя")
    void throwsWhenUserNotFound() {
        Assertions.assertThrows(UserNotFoundException.class, () -> serviceWith(0).grantDaily(USER_ID));
    }
}
