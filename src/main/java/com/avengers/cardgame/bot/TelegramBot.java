package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.ResolvedCommand;
import com.avengers.cardgame.command.CommandRegistry;
import com.avengers.cardgame.command.CommandRequest;
import com.avengers.cardgame.command.Reply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Optional;

/**
 * Telegram-бот.
 */
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    /** Текст ответа на неизвестную команду. */
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Неизвестная команда. Введите /help для списка команд.";

    /** Текст ответа при ошибке обработки. */
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке команды.";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    /** Клиент для отправки сообщений в Telegram. */
    private final TelegramClient telegramClient;

    /** Реестр команд. */
    private final CommandRegistry commandRegistry;

    public TelegramBot(TelegramClient telegramClient, CommandRegistry commandRegistry) {
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

        Reply reply = dispatch(userId, userName, text);

        try {
            SendMessage message = SendMessage.builder()
                    .chatId(chatId)
                    .text(reply.text())
                    .build();
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    private Reply dispatch(long userId, String userName, String text) {
        Optional<ResolvedCommand> resolved = commandRegistry.resolve(text);
        if (resolved.isEmpty()) {
            return new Reply(UNKNOWN_COMMAND_MESSAGE);
        }

        ResolvedCommand rc = resolved.get();
        try {
            return rc.command().handle(new CommandRequest(userId, userName, rc.argument()));
        } catch (Exception e) {
            log.error("Error while executing command {}", text, e);
            return new Reply(ERROR_MESSAGE);
        }
    }
}