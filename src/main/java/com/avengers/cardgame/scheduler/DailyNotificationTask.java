
package com.avengers.cardgame.scheduler;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.messaging.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

public class DailyNotificationTask implements Runnable {
    private static final String NOTIFICATION_MESSAGE =
            "🔔 Ежедневная награда снова доступна!";

    private final Logger log = LoggerFactory.getLogger(DailyNotificationTask.class);
    private final UserRepository userRepository;
    private final Bot bot;
    private final GameConfig gameConfig;
    private final Clock clock;

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
            log.error("Daily notification run failed", exception);
        }
    }

    private void notifyUsers() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime threshold = now.minus(gameConfig.dailyCooldown());

        List<User> users = userRepository.findUsersToNotify(threshold);

        for (User user : users) {
            try {
                bot.sendMessage(
                        new Message(NOTIFICATION_MESSAGE),
                        user.userId()
                );
            } catch (BotException exception) {
                log.warn(
                        "Failed to deliver notification to user {}",
                        user.userId(),
                        exception
                );
                continue;
            }

            try {
                userRepository.updateDailyNotifiedAt(user.userId(), now);
            } catch (UserNotFoundException exception) {
                log.warn(
                        "Failed to mark user {} as notified",
                        user.userId(),
                        exception
                );
            }
        }
    }
}
