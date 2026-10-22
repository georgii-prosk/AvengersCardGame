package com.avengers.cardgame;

import com.avengers.cardgame.bot.TelegramBot;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.command.InventoryRenderer;
import com.avengers.cardgame.command.callback.CallbackPrefix;
import com.avengers.cardgame.command.callback.InventoryPageCallback;
import com.avengers.cardgame.command.callback.ViewCardCallback;
import com.avengers.cardgame.config.AppConfig;
import com.avengers.cardgame.config.ConfigProvider;
import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.DatabaseMigrator;
import com.avengers.cardgame.config.EnvironmentConfigReader;
import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.GameConfigReader;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.repository.HeroRepository;
import com.avengers.cardgame.repository.UserCardRepository;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.repository.impl.PostgresHeroRepository;
import com.avengers.cardgame.repository.impl.PostgresUserCardRepository;
import com.avengers.cardgame.repository.impl.PostgresUserRepository;
import com.avengers.cardgame.scheduler.DailyNotificationScheduler;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.HeroService;
import com.avengers.cardgame.service.HeroSyncService;
import com.avengers.cardgame.service.MessageProcessingService;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.CardServiceImpl;
import com.avengers.cardgame.service.impl.CommandCallbackProcessingService;
import com.avengers.cardgame.service.impl.CommandMessageProcessingService;
import com.avengers.cardgame.service.impl.HeroServiceImpl;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.time.Clock;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Точка входа приложения.
 * <p>
 * Composition root: здесь собирается весь граф зависимостей — конфигурация,
 * подключение к БД, репозитории, сервисы, реестр команд, обработчик сообщений
 * и Telegram-адаптер.
 */
public class AvengersCardGameApplication {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    /**
     * Запускает приложение.
     */
    void main() {
        ConfigProvider configProvider = new EnvironmentConfigReader();
        AppConfig env;
        try {
            env = configProvider.read();
        } catch (AppException e) {
            log.error("Invalid configuration: {}", e.getMessage(), e);
            return;
        }
        GameConfig gameConfig = new GameConfigReader().read();

        // 1. Инфраструктура: миграции и подключение к БД
        new DatabaseMigrator(env.database()).migrate();
        ConnectionFactory connectionFactory = new ConnectionFactory(env.database());

        // 2. Слой доступа к данным
        UserRepository userRepository = new PostgresUserRepository(connectionFactory);
        HeroRepository heroRepository = new PostgresHeroRepository(connectionFactory);
        UserCardRepository userCardRepository = new PostgresUserCardRepository(connectionFactory);

        // 3. Синхронизация справочника героев из JSON
        new HeroSyncService(heroRepository, new ObjectMapper()).sync();

        // 4. Бизнес-логика
        UserService userService = new UserServiceImpl(userRepository);
        HeroService heroService = new HeroServiceImpl(heroRepository, new Random());
        CardService cardService = new CardServiceImpl(
                userService, userCardRepository, heroService,
                gameConfig, new Random(), Clock.systemUTC());

        // 5. Слой команд
        InventoryRenderer renderer = new InventoryRenderer(userCardRepository, heroRepository);
        CommandRegistry commandRegistry = new CommandRegistryFactory(
                userService, cardService, userCardRepository, renderer).create();
        MessageProcessingService logic = new CommandMessageProcessingService(commandRegistry);

        // 6. Слой callback-обработчиков
        CommandCallbackProcessingService callbackLogic = new CommandCallbackProcessingService();
        callbackLogic.register(CallbackPrefix.VIEW_CARD.value(),
                new ViewCardCallback(heroRepository, userCardRepository));
        callbackLogic.register(CallbackPrefix.PAGE.value(),
                new InventoryPageCallback(renderer));

        // 7. Транспорт
        TelegramBot bot = new TelegramBot(env.botToken(), logic, callbackLogic);

        // 8. Планировщик уведомлений
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        DailyNotificationScheduler scheduler = new DailyNotificationScheduler(
                userRepository, bot, gameConfig, executor, Clock.systemUTC());
        scheduler.start();

        // 9. Запуск
        startBot(env.botToken(), bot);
    }

    /**
     * Регистрирует бота в Telegram и держит приложение запущенным.
     *
     * @param botToken токен бота
     * @param bot      адаптер бота
     */
    private void startBot(String botToken, TelegramBot bot) {
        try (TelegramBotsLongPollingApplication application =
                     new TelegramBotsLongPollingApplication()) {

            application.registerBot(botToken, bot);
            log.info("AvengersCardGame bot started");

            Thread.currentThread().join();

        } catch (InterruptedException e) {
            log.warn("Main thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error while running the bot", e);
        }
    }
}