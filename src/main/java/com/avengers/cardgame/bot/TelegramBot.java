package com.avengers.cardgame.bot;

import com.avengers.cardgame.command.Reply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Telegram-адаптер.
 * <p>
 * Принимает обновления от Telegram, передаёт их в {@link MessageDispatcher}
 * и отправляет ответ обратно. Бизнес-логики не содержит.
 */
public class TelegramBot implements LongPollingSingleThreadUpdateConsumer {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    /** Клиент для отправки сообщений в Telegram. */
    private final TelegramClient telegramClient;

    /** Обработчик сообщений. */
    private final MessageDispatcher dispatcher;

    /**
     * @param telegramClient клиент для отправки сообщений в Telegram
     * @param dispatcher     обработчик сообщений
     */
    public TelegramBot(TelegramClient telegramClient, MessageDispatcher dispatcher) {
        this.telegramClient = telegramClient;
        this.dispatcher = dispatcher;
    }

    /**
     * Обрабатывает обновление от Telegram.
     *
     * @param update обновление
     */
    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();

        Reply reply = dispatcher.dispatch(userId, userName, text);

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
}