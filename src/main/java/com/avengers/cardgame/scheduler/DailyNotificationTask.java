package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.UserNotFoundException;
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
    /** Шаблон уведомления */
    private static final String NOTIFICATION_MESSAGE =
            "🔔 Ежедневная награда снова доступна!";

    /** Логгер. */
    private final Logger logger = LoggerFactory.getLogger(DailyNotificationTask.class);
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
            logger.error("Daily notification run failed", exception);
        }
    }

    /**
     * Находит пользователей, готовых к получению награды, и уведомляет их.
     *
     * @throws UserNotFoundException если пользователь был удален до обновления статуса
     */
    private void notifyUsers() throws UserNotFoundException {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.minus(gameConfig.dailyCooldown());

        List<User> users = userRepository.findUsersToNotify(threshold);

        for (User user : users) {
            bot.sendMessage(new Message(NOTIFICATION_MESSAGE), user.userId());
            userRepository.updateDailyNotifiedAt(user.userId(), now);
        }
    }
}