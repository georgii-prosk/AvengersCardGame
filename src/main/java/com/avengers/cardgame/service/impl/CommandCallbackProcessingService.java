package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.bot.Bot;
import com.avengers.cardgame.command.callback.CallbackContext;
import com.avengers.cardgame.command.callback.CallbackHandler;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.CallbackProcessingService;
import com.avengers.cardgame.service.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация {@link CallbackProcessingService}: маршрутизирует нажатия
 * на кнопки по префиксу данных ({@code prefix:argument}).
 */
public class CommandCallbackProcessingService implements CallbackProcessingService {

    /** Текст ответа при доменной ошибке. */
    private static final String ERROR_MESSAGE =
            "Произошла ошибка при обработке нажатия.";

    /** Разделитель префикса и аргумента в данных кнопки. */
    private static final String SEPARATOR = ":";

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(CommandCallbackProcessingService.class);

    /** Обработчики: префикс данных → обработчик. */
    private final Map<String, CallbackHandler> handlers = new HashMap<>();

    /**
     * Регистрирует обработчик для префикса данных кнопки.
     *
     * @param prefix  префикс, например {@code "view_card"}
     * @param handler обработчик
     */
    public void register(String prefix, CallbackHandler handler) {
        handlers.put(prefix, handler);
    }

    @Override
    public void processCallback(long userId, long chatId, String callbackData, Bot bot) {
        if (callbackData == null || callbackData.isBlank()) {
            log.debug("Empty callback data from user {}", userId);
            return;
        }

        String prefix = extractPrefix(callbackData);
        CallbackHandler handler = handlers.get(prefix);
        if (handler == null) {
            log.debug("Unknown callback: {}", callbackData);
            return;
        }

        try {
            handler.handle(new CallbackContext(userId, chatId, callbackData, bot));
        } catch (BotException e) {
            // Бот сломан — отвечать через него бессмысленно.
            log.error("Bot error while handling callback", e);
        } catch (AppException e) {
            log.error("Domain error while handling callback", e);
            sendSafely(bot, new Message(ERROR_MESSAGE), chatId);
        }
    }

    /**
     * Пытается отправить сообщение и логирует ошибку, если это не удалось.
     *
     * @param bot     бот
     * @param message сообщение
     * @param chatId  идентификатор чата
     */
    private void sendSafely(Bot bot, Message message, long chatId) {
        try {
            bot.sendMessage(message, chatId);
        } catch (BotException e) {
            log.error("Failed to send message to chat {}", chatId, e);
        }
    }

    /**
     * Выделяет префикс из данных кнопки.
     *
     * @param callbackData данные кнопки
     * @return часть строки до первого разделителя (или вся строка)
     */
    private String extractPrefix(String callbackData) {
        int index = callbackData.indexOf(SEPARATOR);
        return index == -1 ? callbackData : callbackData.substring(0, index);
    }
}