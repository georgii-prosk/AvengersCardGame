package com.avengers.cardgame;

import com.avengers.cardgame.bot.TelegramBot;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.command.InventoryRenderer;
import com.avengers.cardgame.command.callback.CallbackPrefix;
import com.avengers.cardgame.command.callback.InventoryPageCallback;
import com.avengers.cardgame.command.callback.ViewCardCallback;
import com.avengers.cardgame.config.*;
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

/**
 * Точка входа приложения.
 */
public class AvengersCardGameApplication {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    /**
     * Запускает приложение.
     */
    void main() {
        ConfigProvider configProvider = new EnvironmentConfigReader();
        AppConfig env = configProvider.read();
        GameConfig gameConfig = new GameConfigReader().read();

        // 1. Инфраструктура: миграции и подключение к БД
        new DatabaseMigrator(env.database()).migrate();
        ConnectionFactory connectionFactory = new ConnectionFactory(env.database());

        // 2. Слой доступа к данным
        UserRepository userRepository = new PostgresUserRepository(connectionFactory);
        HeroRepository heroRepository = new PostgresHeroRepository(connectionFactory);
        UserCardRepository userCardRepository = new PostgresUserCardRepository(connectionFactory);

        // 3. Синхронизация справочника героев (выполняется при каждом старте)
        ObjectMapper objectMapper = new ObjectMapper();
        new HeroSyncService(heroRepository, objectMapper).sync();

        // 4. Бизнес-логика
        Random random = new Random();
        UserService userService = new UserServiceImpl(userRepository);
        HeroService heroService = new HeroServiceImpl(heroRepository, random);
        CardService cardService = new CardServiceImpl(
                userService, userCardRepository, heroService, gameConfig, random, Clock.systemDefaultZone());

        // 5. Слой команд
        InventoryRenderer inventoryRenderer = new InventoryRenderer(userCardRepository, heroRepository);
        CommandRegistry commandRegistry = new CommandRegistryFactory(
                userService, cardService, userCardRepository, inventoryRenderer).create();
        MessageProcessingService logic = new CommandMessageProcessingService(commandRegistry);

        CommandCallbackProcessingService callbackLogic = new CommandCallbackProcessingService();
        callbackLogic.register(CallbackPrefix.VIEW_CARD.value(),
                new ViewCardCallback(heroRepository, userCardRepository));
        callbackLogic.register(CallbackPrefix.PAGE.value(), new InventoryPageCallback(inventoryRenderer));

        // 6. Транспорт
        TelegramBot bot = new TelegramBot(env.botToken(), logic, callbackLogic);

        // 7. Фоновые задачи
        DailyNotificationScheduler scheduler = new DailyNotificationScheduler(
                userRepository, bot, gameConfig,
                Executors.newSingleThreadScheduledExecutor(), Clock.systemDefaultZone());
        scheduler.start();

        // 8. Запуск
        try {
            startBot(env.botToken(), bot);
        } finally {
            scheduler.stop();
        }
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