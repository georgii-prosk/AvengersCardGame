package com.avengers.cardgame;

import com.avengers.cardgame.bot.AvengersBot;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.config.DatabaseConfig;
import com.avengers.cardgame.config.FlywayConfig;
import com.avengers.cardgame.repository.UserRepository;
import com.avengers.cardgame.repository.impl.UserRepositoryImpl;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Точка входа приложения и composition root.
 * <p>
 * Здесь собирается весь граф зависимостей: конфигурация БД, миграции,
 * репозитории, сервисы, реестр команд, Telegram-клиент и сам бот.
 * <p>
 * Ни один класс ниже по потоку не знает, как создаются его зависимости —
 * они получают их готовыми через конструктор.
 */
public class AvengersCardGameApplication {

    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    void main() {
        String botToken = System.getenv("BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            log.error("Environment variable BOT_TOKEN is not set");
            return;
        }

        // 1. Инфраструктура: миграции и подключение к БД
        new FlywayConfig().migrate();
        DatabaseConfig dbConfig = new DatabaseConfig();

        // 2. Слой доступа к данным
        UserRepository userRepository = new UserRepositoryImpl(dbConfig);

        // 3. Бизнес-логика
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();

        // 4. Слой команд
        CommandRegistry commandRegistry = new CommandRegistry(userService, validationService);

        // 5. Транспорт: клиент Telegram и сам бот
        TelegramClient telegramClient = new OkHttpTelegramClient(botToken);
        AvengersBot bot = new AvengersBot(telegramClient, commandRegistry);

        // 6. Запуск long polling
        startBot(botToken, bot);
    }

    /**
     * Регистрирует бота в Telegram и держит приложение запущенным.
     *
     * @param botToken токен бота
     * @param bot      объект-обработчик апдейтов
     */
    private void startBot(String botToken, AvengersBot bot) {
        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(botToken, bot);
            log.info("AvengersCardGame bot started");

            Thread.currentThread().join();

        } catch (TelegramApiException e) {
            log.error("Failed to register the bot", e);
        } catch (InterruptedException e) {
            log.warn("Main thread was interrupted", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Unexpected error while running the bot", e);
        }
    }
}