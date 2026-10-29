package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/** Тесты для {@link DailyNotificationTask}. */
class DailyNotificationTaskTest {

    private static final Instant NOW_INSTANT = Instant.parse("2026-10-21T12:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(NOW_INSTANT, ZoneOffset.UTC);
    private static final String NOTIFICATION = "🔔 Ежедневная награда снова доступна!";

    private InMemoryUserRepository userRepository;
    private FakeBot bot;
    private GameConfig config;
    private Clock clock;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        bot = new FakeBot();
        config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));
        clock = Clock.fixed(NOW_INSTANT, ZoneOffset.UTC);
    }

    private DailyNotificationTask task(Bot taskBot) {
        return new DailyNotificationTask(userRepository, taskBot, config, clock);
    }

    private void saveUser(long id, LocalDateTime lastDailyAt, LocalDateTime notifiedAt) throws Exception {
        userRepository.save(User.of(id, PlayerName.of("Hero"), null, 0, 0,
                null, null, lastDailyAt, notifiedAt));
    }

    @Test
    @DisplayName("Уведомляет игрока, у которого прошёл кулдаун")
    void notifiesUserWhenCooldownPassed() throws Exception {
        saveUser(1L, NOW.minusHours(25), null);

        task(bot).run();

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(NOTIFICATION, bot.lastSent().message().text());
        Assertions.assertEquals(1L, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Уведомляет ровно через 24 часа после награды")
    void notifiesExactlyAtCooldownBoundary() throws Exception {
        saveUser(1L, NOW.minusHours(24), null);

        task(bot).run();

        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Не уведомляет, пока кулдаун не прошёл")
    void doesNotNotifyBeforeCooldown() throws Exception {
        saveUser(1L, NOW.minusHours(23), null);

        task(bot).run();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Не уведомляет игроков, ещё ни разу не получавших /daily")
    void doesNotNotifyUsersWithoutDaily() throws Exception {
        saveUser(1L, null, null);

        task(bot).run();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Записывает время уведомления и не отправляет повторно")
    void marksNotifiedAndDoesNotRepeat() throws Exception {
        saveUser(1L, NOW.minusHours(30), null);
        DailyNotificationTask task = task(bot);

        task.run();
        task.run();

        Assertions.assertEquals(1, bot.count());
        Assertions.assertEquals(NOW, userRepository.findByUserId(1L).orElseThrow().dailyNotifiedAt());
    }

    @Test
    @DisplayName("Не уведомляет, если уведомление уже было после последней награды")
    void skipsAlreadyNotified() throws Exception {
        saveUser(1L, NOW.minusHours(30), NOW.minusHours(5));

        task(bot).run();

        Assertions.assertEquals(0, bot.count());
    }

    @Test
    @DisplayName("Уведомляет снова после новой награды")
    void notifiesAgainAfterNewDaily() throws Exception {
        saveUser(1L, NOW.minusHours(25), NOW.minusHours(50));

        task(bot).run();

        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Ошибка отправки одному игроку не мешает остальным")
    void failureForOneUserDoesNotStopOthers() throws Exception {
        saveUser(1L, NOW.minusHours(30), null);
        saveUser(2L, NOW.minusHours(30), null);
        FakeBot delivering = new FakeBot();
        Bot flaky = (message, chatId) -> {
            if (chatId == 1L) {
                throw new BotException("Forbidden: bot was blocked by the user", null);
            }
            delivering.sendMessage(message, chatId);
        };

        task(flaky).run();

        Assertions.assertEquals(1, delivering.count());
        Assertions.assertEquals(2L, delivering.sent().getFirst().chatId());
        Assertions.assertNull(userRepository.findByUserId(1L).orElseThrow().dailyNotifiedAt());
        Assertions.assertNotNull(userRepository.findByUserId(2L).orElseThrow().dailyNotifiedAt());
    }
}