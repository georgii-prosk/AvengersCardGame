package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Задача для периодической отправки уведомлений о доступности ежедневной награды.
 */
public class DailyNotificationTask implements Runnable {
    private static final String NOTIFICATION_MESSAGE =
            "🔔 Ежедневная награда снова доступна!";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(DailyNotificationTask.class);
    /** Репозиторий пользователей. */
    private final UserRepository userRepository;
    /** Бот для отправки сообщений. */
    private final Bot bot;
    /** Игровая конфигурация. */
    private final GameConfig gameConfig;
    /** Часы для получения текущего времени. */
    private final Clock clock;

    /**
     * @param userRepository репозиторий для поиска пользователей
     * @param bot            бот для отправки сообщений
     * @param gameConfig     конфигурация для расчета времени кулдауна
     * @param clock          часы для получения текущего времени
     */
    public DailyNotificationTask(
            UserRepository userRepository,
            Bot bot,
            GameConfig gameConfig,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.bot = bot;
        this.gameConfig = gameConfig;
        this.clock = clock;
    }

    @Override
    public void run() {
        try {
            notifyUsers();
        } catch (Exception exception) {
            // Ловим все исключения, чтобы падение задачи не остановило планировщик
            log.error("Daily notification run failed", exception);
        }
    }

    /**
     * Находит пользователей, готовых к получению награды, и уведомляет их.
     * Ошибка отправки одному пользователю не прерывает цикл для остальных.
     */
    private void notifyUsers() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.minus(gameConfig.dailyCooldown());

        List<User> users = userRepository.findUsersToNotify(threshold);

        for (User user : users) {
            try {
                bot.sendMessage(new Message(NOTIFICATION_MESSAGE), user.userId());
                userRepository.updateDailyNotifiedAt(user.userId(), now);
            } catch (Exception exception) {
                log.warn("Failed to process notification for user {}", user.userId(), exception);
            }
        }
    }
}