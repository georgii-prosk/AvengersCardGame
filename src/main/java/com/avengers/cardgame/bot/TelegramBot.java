package com.avengers.cardgame.bot;

import com.avengers.cardgame.service.Button;
import com.avengers.cardgame.service.CallbackProcessingService;
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

/**
 * Telegram-адаптер.
 * <p>
 * Принимает обновления от Telegram, передаёт данные в
 * {@link MessageProcessingService} (текст) и {@link CallbackProcessingService}
 * (нажатия на inline-кнопки) и отправляет ответы через {@link TelegramClient}. Бизнес-логики не содержит.
 */
public final class TelegramBot extends DefaultLongPollingUpdateConsumer implements Bot {

    /** Логгер. */
    private static final Logger log = LoggerFactory.getLogger(TelegramBot.class);

    /** Клиент для отправки сообщений в Telegram. */
    private final TelegramClient telegramClient;

    /** Обработчик сообщений. */
    private final MessageProcessingService logic;

    /** Обработчик нажатий на inline-кнопки. */
    private final CallbackProcessingService callbackLogic;

    /**
     * @param token         токен Telegram-бота
     * @param logic         сервис обработки сообщений
     * @param callbackLogic сервис обработки нажатий на кнопки
     */
    public TelegramBot(String token, MessageProcessingService logic, CallbackProcessingService callbackLogic) {
        this.telegramClient = new OkHttpTelegramClient(token);
        this.logic = logic;
        this.callbackLogic = callbackLogic;
    }

    @Override
    public void sendMessage(@NotNull Message msg, long chatId) {
        try {
            SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                    .chatId(chatId)
                    .text(msg.text());
            if (!msg.buttons().isEmpty()) {
                builder.replyMarkup(toKeyboard(msg.buttons()));
            }
            telegramClient.execute(builder.build());
        } catch (TelegramApiException e) {
            log.error("Failed to send message", e);
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
            return;
        }
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        long userId = update.getMessage().getFrom().getId();
        String userName = update.getMessage().getFrom().getUserName();
        String text = update.getMessage().getText();

        logic.processMessage(userId, userName, chatId, text, this);
    }

    /**
     * Обрабатывает нажатие на inline-кнопку: снимает индикатор загрузки на кнопке
     * и передаёт данные в {@link CallbackProcessingService}.
     *
     * @param callbackQuery нажатие на кнопку
     */
    private void handleCallback(CallbackQuery callbackQuery) {
        acknowledge(callbackQuery.getId());

        if (callbackQuery.getMessage() == null || callbackQuery.getData() == null) {
            return;
        }

        long chatId = callbackQuery.getMessage().getChatId();
        long userId = callbackQuery.getFrom().getId();

        callbackLogic.processCallback(userId, chatId, callbackQuery.getData(), this);
    }

    /**
     * Подтверждает получение нажатия, чтобы у кнопки пропала анимация ожидания.
     *
     * @param callbackQueryId идентификатор нажатия
     */
    private void acknowledge(String callbackQueryId) {
        try {
            telegramClient.execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback query", e);
        }
    }

    /**
     * Преобразует ряды кнопок в inline-клавиатуру Telegram.
     *
     * @param rows ряды кнопок
     * @return клавиатура для прикрепления к сообщению
     */
    private InlineKeyboardMarkup toKeyboard(List<List<Button>> rows) {
        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        for (List<Button> row : rows) {
            InlineKeyboardRow keyboardRow = new InlineKeyboardRow();
            for (Button button : row) {
                keyboardRow.add(InlineKeyboardButton.builder()
                        .text(button.text())
                        .callbackData(button.callbackData())
                        .build());
            }
            keyboard.add(keyboardRow);
        }
        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }
}
