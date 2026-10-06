package com.avengers.cardgame;

import com.avengers.cardgame.bot.AvengersBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.flywaydb.core.Flyway;

public class AvengersCardGameApplication {

    private final Logger log = LoggerFactory.getLogger(AvengersCardGameApplication.class);

    void main() {
        String botToken = System.getenv("BOT_TOKEN");

        if (botToken == null || botToken.isBlank()) {
            log.error("Environment variable BOT_TOKEN is not set");
            return;
        }

        Flyway flyway = Flyway.configure()
                .dataSource(
                        System.getenv("DB_URL"),
                        System.getenv("DB_USER"),
                        System.getenv("DB_PASSWORD")
                )
                .load();
        flyway.migrate();
        log.info("Database migrations applied");

        try (TelegramBotsLongPollingApplication botsApplication =
                     new TelegramBotsLongPollingApplication()) {

            botsApplication.registerBot(botToken, new AvengersBot(botToken));
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