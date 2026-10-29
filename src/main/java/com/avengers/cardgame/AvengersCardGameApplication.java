package com.avengers.cardgame;

import com.avengers.cardgame.bot.TelegramBot;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.command.InventoryRenderer;
import com.avengers.cardgame.config.AppConfig;
import com.avengers.cardgame.config.ConfigProvider;
import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.DatabaseMigrator;
import com.avengers.cardgame.config.EnvironmentConfigReader;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.presentation.HeroRenderer;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.repository.impl.PostgresUserCardRepository;
import com.avengers.cardgame.repository.impl.PostgresUserRepository;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.CommandRegistry;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.service.MessageProcessingService;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.CardServiceImpl;
import com.avengers.cardgame.service.impl.CommandMessageProcessingService;
import com.avengers.cardgame.service.impl.HeroServiceImpl;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import java.time.Clock;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import com.avengers.cardgame.scheduler.DailyNotificationTask;
import com.avengers.cardgame.scheduler.TaskScheduler;
import com.avengers.cardgame.repository.impl.InMemoryHeroRepository;

/**
 * Точка входа приложения (Composition Root).
 * Здесь вручную собираются все зависимости: конфигурация, БД, репозитории, сервисы,
 * обработчики команд и запускается сам бот.
 */
public class AvengersCardGameApplication {
    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    void main() {
        // 1. Загрузка конфигурации
        ConfigProvider configProvider = new EnvironmentConfigReader();
        AppConfig config;
        try {
            config = configProvider.read();
        } catch (AppException exception) {
            log.error("Invalid configuration: {}", exception.getMessage());
            return;
        }

        // 2. Инициализация инфраструктуры
        GameConfig gameConfig = config.game();
        new DatabaseMigrator(config.database()).migrate();
        ConnectionFactory connectionFactory = new ConnectionFactory(config.database());

        // 3. Создание репозиториев
        UserRepository userRepository = new PostgresUserRepository(connectionFactory);
        HeroRepository heroRepository = new InMemoryHeroRepository(); // Герои загружаются из JSON
        UserCardRepository userCardRepository = new PostgresUserCardRepository(connectionFactory);

        // 4. Создание сервисов
        UserService userService = new UserServiceImpl(userRepository);
        HeroService heroService = new HeroServiceImpl(heroRepository, new Random());
        CardService cardService = new CardServiceImpl(userService, userCardRepository,
                heroService, gameConfig, new Random(), Clock.systemUTC());

        // 5. Настройка обработки команд
        HeroRenderer heroRenderer = new HeroRenderer();
        InventoryRenderer inventoryRenderer = new InventoryRenderer(userCardRepository, heroRepository, heroRenderer);

        CommandRegistry registry = new CommandRegistryFactory(userService, cardService,
                userCardRepository, heroRepository, inventoryRenderer, heroRenderer).create();

        MessageProcessingService logic = new CommandMessageProcessingService(registry);
        TelegramBot bot = new TelegramBot(config.botToken(), logic);

        // 6. Настройка планировщика фоновых задач
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        TaskScheduler taskScheduler = new TaskScheduler(executor);
        DailyNotificationTask dailyNotificationTask = new DailyNotificationTask(
                userRepository, bot, gameConfig, Clock.systemUTC()
        );

        taskScheduler.scheduleWithFixedDelay(dailyNotificationTask, gameConfig.notificationInterval());

        // Корректная остановка планировщика при выключении приложения
        Runtime.getRuntime().addShutdownHook(
                new Thread(taskScheduler::stop, "scheduler-shutdown")
        );

        // 7. Запуск бота
        startBot(config.botToken(), bot);
    }

    /**
     * Регистрирует бота в Telegram и блокирует текущий поток,
     * поддерживая работу приложения до его принудительной остановки.
     */
    private void startBot(String botToken, TelegramBot bot) {
        try (TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication()) {
            application.registerBot(botToken, bot);
            log.info("AvengersCardGame bot started");
            // Блокируем главный поток навсегда, пока приложение не будет остановлено
            Thread.currentThread().join();
        } catch (InterruptedException exception) {
            log.warn("Main thread was interrupted", exception);
            Thread.currentThread().interrupt();
        } catch (Exception exception) {
            log.error("Unexpected error while running the bot", exception);
        }
    }
}