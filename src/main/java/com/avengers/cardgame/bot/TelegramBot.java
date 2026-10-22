package com.avengers.cardgame.bot;

import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.MessageProcessingService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Telegram-адаптер.
 * <p>
 * Принимает обновления от Telegram, передаёт данные в
 * {@link MessageProcessingService} и отправляет ответы через
 * {@link TelegramClient}. Бизнес-логики не содержит.
 */
public final class TelegramBot extends DefaultLongPollingUpdateConsumer implements Bot {

    /** Логгер. */
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    /** Клиент для отправки сообщений в Telegram. */
    private final TelegramClient telegramClient;

    /** Обработчик сообщений. */
    private final MessageProcessingService logic;

    /**
     * @param token токен Telegram-бота
     * @param logic сервис обработки сообщений
     */
    public TelegramBot(String token, MessageProcessingService logic) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.logic = logic;
    }

    @Override
    public void sendMessage(@NotNull Message msg, long chatId) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                    .chatId(chatId)
                    .text(msg.text())
                    .build();
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
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

        logic.processMessage(userId, userName, chatId, text, this);
    }
}