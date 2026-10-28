package com.avengers.cardgame.bot;

import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.Button;
import com.avengers.cardgame.service.Message;
import com.avengers.cardgame.service.MessageProcessingService;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.DefaultLongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Telegram-адаптер: передаёт сообщения и callback-данные общему обработчику команд. */
public final class TelegramBot extends DefaultLongPollingUpdateConsumer implements Bot {
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);
    private static final int MAX_SEND_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MILLIS = 500;
    private final TelegramClient telegramClient;
    private final MessageProcessingService logic;

    public TelegramBot(String token, MessageProcessingService logic) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.logic = logic;
    }

    @Override
    public void sendMessage(@NotNull Message message, long chatId) {
        SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder().chatId(chatId).text(message.text());
        if (!message.buttons().isEmpty()) builder.replyMarkup(toKeyboard(message.buttons()));
        SendMessage request = builder.build();
        for (int attempt = 1; attempt <= MAX_SEND_ATTEMPTS; attempt++) {
            try {
                telegramClient.execute(request);
                return;
            } catch (TelegramApiException exception) {
                if (attempt == MAX_SEND_ATTEMPTS) {
                    log.error("Failed to send message to chat {} after {} attempts", chatId, MAX_SEND_ATTEMPTS, exception);
                    throw new BotException("Failed to send message to chat " + chatId, exception);
                }
                log.warn("Failed to send message to chat {}. Attempt {}/{}", chatId, attempt, MAX_SEND_ATTEMPTS, exception);
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MILLIS * attempt);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new BotException("Interrupted while waiting to retry message sending", interrupted);
                }
            }
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) return;
        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        logic.processMessage(userId, userName, chatId, update.getMessage().getText(), this);
    }

    private void handleCallback(CallbackQuery query) {
        acknowledgeCallbackQuery(query.getId());
        if (query.getMessage() == null || query.getData() == null) return;
        logic.processMessage(query.getFrom().getId(), query.getFrom().getUserName(),
                query.getMessage().getChatId(), query.getData(), this);
    }

    private void acknowledgeCallbackQuery(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder().callbackQueryId(callbackQueryId).build());
        } catch (TelegramApiException exception) {
            log.error("Failed to answer callback query", exception);
        }
    }

    private InlineKeyboardMarkup toKeyboard(List<List<Button>> rows) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (List<Button> row : rows) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (Button button : row) {
                keyboardRow.add(InlineKeyboardButton.builder().text(button.text())
                        .callbackData(button.callbackData()).build());
            }
            keyboard.add(keyboardRow);
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
