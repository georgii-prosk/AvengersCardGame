package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Фоновый планировщик: напоминает игрокам, что ежедневная награда снова доступна.
 */
public class DailyNotificationScheduler {

    /** Текст уведомления. */
    private static final String NOTIFICATION_MESSAGE = "🔔 Ежедневная награда снова доступна!";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(DailyNotificationScheduler.class);

    /** Репозиторий пользователей. */
    private final UserRepository userRepository;

    /** Бот для отправки уведомлений. */
    private final Bot bot;

    /** Игровая конфигурация. */
    private final GameConfig gameConfig;

    /** Исполнитель периодической задачи. */
    private final ScheduledExecutorService executor;

    /** Часы. */
    private final Clock clock;

    /**
     * @param userRepository репозиторий пользователей
     * @param bot            бот для отправки уведомлений
     * @param gameConfig     игровая конфигурация
     * @param executor       исполнитель периодической задачи
     * @param clock          часы
     */
    public DailyNotificationScheduler(
            UserRepository userRepository,
            Bot bot,
            GameConfig gameConfig,
            ScheduledExecutorService executor,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.bot = bot;
        this.gameConfig = gameConfig;
        this.executor = executor;
        this.clock = clock;
    }

    /**
     * Запускает периодическую проверку с интервалом из {@link GameConfig}.
     */
    public void start() {
        long intervalSeconds = gameConfig.notificationInterval().toSeconds();
        executor.scheduleWithFixedDelay(this::runSafely, 0, intervalSeconds, TimeUnit.SECONDS);
        log.info("Daily notification scheduler started, interval: {} s", intervalSeconds);
    }

    /**
     * Останавливает планировщик.
     */
    public void stop() {
        executor.shutdown();
    }

    /**
     * Находит игроков, у которых награда стала доступна, и отправляет им
     * уведомление. Сбой на одном игроке (например, он заблокировал бота)
     * не прерывает обработку остальных. Время уведомления записывается
     * только после успешной доставки, иначе попытка повторится при следующем запуске.
     */
    public void notifyUsers() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.minus(gameConfig.dailyCooldown());

        List<User> users = userRepository.findUsersToNotify(threshold);
        for (User user : users) {
            try {
                // В личных чатах Telegram идентификатор чата совпадает с идентификатором пользователя.
                bot.sendMessage(new Message(NOTIFICATION_MESSAGE), user.userId());
            } catch (BotException e) {
                log.warn("Failed to deliver notification to user {}", user.userId(), e);
                continue;
            }

            try {
                userRepository.updateDailyNotifiedAt(user.userId(), now);
            } catch (UserNotFoundException e) {
                log.warn("Failed to mark user {} as notified", user.userId(), e);
            }
        }
    }

    /**
     * Запускает {@link #notifyUsers()} и гарантирует, что исключение не
     * отменит дальнейшие запуски периодической задачи.
     */
    private void runSafely() {
        try {
            notifyUsers();
        } catch (Exception e) {
            log.error("Daily notification run failed", e);
        }
    }
}
