package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.Command;
import com.avengers.cardgame.command.CommandRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Telegram-бот AvengersCardGame.
 * <p>
 * Принимает {@link TelegramClient} и {@link CommandRegistry}
 */
public class AvengersBot implements LongPollingSingleThreadUpdateConsumer {

    private final Logger log = LoggerFactory.getLogger(AvengersBot.class);

    private final TelegramClient telegramClient;
    private final CommandRegistry commandRegistry;

    /**
     * @param telegramClient клиент для отправки сообщений в Telegram
     * @param commandRegistry реестр команд для обработки входящих сообщений
     */
    public AvengersBot(TelegramClient telegramClient, CommandRegistry commandRegistry) {
        this.telegramClient = telegramClient;
        this.commandRegistry = commandRegistry;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();

        Command command = commandRegistry.resolve(text);
        try {
            command.execute(telegramClient, chatId, userId, userName, text);
        } catch (TelegramApiException e) {
            log.error("Error while executing the command", e);
        }
    }
}