package com.avengers.cardgame;

import com.avengers.cardgame.bot.TelegramBot;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRegistryFactory;
import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.DatabaseMigrator;
import com.avengers.cardgame.config.EnvironmentConfig;
import com.avengers.cardgame.config.EnvironmentConfigReader;
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
 * Точка входа приложения.
 */
public class AvengersCardGameApplication {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    void main() {
        EnvironmentConfig env = new EnvironmentConfigReader().read();

        // 1. Инфраструктура: миграции и подключение к БД
        new DatabaseMigrator(env.database()).migrate();
        ConnectionFactory connectionFactory = new ConnectionFactory(env.database());

        // 2. Слой доступа к данным
        UserRepository userRepository = new UserRepositoryImpl(connectionFactory);

        // 3. Бизнес-логика
        UserService userService = new UserService(userRepository);
        ValidationService validationService = new ValidationService();

        // 4. Слой команд
        CommandRegistry commandRegistry =
                new CommandRegistryFactory(userService, validationService).create();

        // 5. Транспорт
        TelegramClient telegramClient = new OkHttpTelegramClient(env.botToken());
        TelegramBot bot = new TelegramBot(telegramClient, commandRegistry);

        // 6. Запуск
        startBot(env.botToken(), bot);
    }

    /**
     * Регистрирует бота в Telegram и держит приложение запущенным.
     *
     * @param botToken токен бота
     * @param bot      объект-обработчик апдейтов
     */
    private void startBot(String botToken, TelegramBot bot) {
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