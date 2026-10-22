package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;                 // уже есть
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Тесты для {@link DailyNotificationScheduler}. Поток планировщика не запускается:
 * проверяется только логика {@code notifyUsers()}.
 */
class DailyNotificationSchedulerTest {

    private static final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC);
    private static final String NOTIFICATION = "🔔 Ежедневная награда снова доступна!";

    private InMemoryUserRepository userRepository;
    private FakeBot bot;
    private ScheduledExecutorService executor;
    private GameConfig config;
    private Clock clock;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        bot = new FakeBot();
        executor = Executors.newSingleThreadScheduledExecutor();
        config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));
        clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);
    }

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    private DailyNotificationScheduler scheduler(Bot b) {
        return new DailyNotificationScheduler(userRepository, b, config, executor, clock);
    }

    /**
     * Сохраняет пользователя с заданными временами последней награды и уведомления.
     *
     * @param id           идентификатор пользователя
     * @param lastDailyAt  время последней награды или {@code null}
     * @param notifiedAt   время последнего уведомления или {@code null}
     * @throws Exception если сохранить пользователя не удалось
     */
    private void saveUser(long id, LocalDateTime lastDailyAt, LocalDateTime notifiedAt) throws Exception {
        User user = User.of(
                id,
                PlayerName.of("Hero"),
                null,           // bio
                0,              // balance
                0,              // cardCount
                null,           // createdAt
                null,           // updatedAt
                lastDailyAt,
                notifiedAt
        );
        userRepository.save(user);
    }

    @Test
    @DisplayName("Уведомляет игрока, у которого прошёл кулдаун")
    void notifiesUserWhenCooldownPassed() throws Exception {
        saveUser(1L, NOW.minusHours(25), null);

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(NOTIFICATION, bot.lastSent().message().text());
        Assertions.assertEquals(1L, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Уведомляет ровно через 24 часа после награды")
    void notifiesExactlyAtCooldownBoundary() throws Exception {
        saveUser(1L, NOW.minusHours(24), null);

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Не уведомляет, пока кулдаун не прошёл")
    void doesNotNotifyBeforeCooldown() throws Exception {
        saveUser(1L, NOW.minusHours(23), null);

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Не уведомляет игроков, ещё ни разу не получавших /daily")
    void doesNotNotifyUsersWithoutDaily() throws Exception {
        saveUser(1L, null, null);

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Записывает время уведомления и не отправляет повторно")
    void marksNotifiedAndDoesNotRepeat() throws Exception {
        saveUser(1L, NOW.minusHours(30), null);
        DailyNotificationScheduler scheduler = scheduler(bot);

        scheduler.notifyUsers();
        scheduler.notifyUsers();

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(NOW, userRepository.findByUserId(1L).orElseThrow().dailyNotifiedAt());
    }

    @Test
    @DisplayName("Не уведомляет, если уведомление уже было после последней награды")
    void skipsAlreadyNotified() throws Exception {
        saveUser(1L, NOW.minusHours(30), NOW.minusHours(5));

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Уведомляет снова после новой награды")
    void notifiesAgainAfterNewDaily() throws Exception {
        saveUser(1L, NOW.minusHours(25), NOW.minusHours(50));

        scheduler(bot).notifyUsers();

        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Ошибка отправки одному игроку не мешает остальным")
    void failureForOneUserDoesNotStopOthers() throws Exception {
        saveUser(1L, NOW.minusHours(30), null);
        saveUser(2L, NOW.minusHours(30), null);
        FakeBot delivering = new FakeBot();
        Bot flaky = (msg, chatId) -> {
            if (chatId == 1L) {
                throw new RuntimeException("Forbidden: bot was blocked by the user");
            }
            delivering.sendMessage(msg, chatId);
        };

        scheduler(flaky).notifyUsers();

        Assertions.assertEquals(1, delivering.count());
        Assertions.assertEquals(2L, delivering.sent().getFirst().chatId());
        Assertions.assertNull(userRepository.findByUserId(1L).orElseThrow().dailyNotifiedAt());
        Assertions.assertNotNull(userRepository.findByUserId(2L).orElseThrow().dailyNotifiedAt());
    }
}
