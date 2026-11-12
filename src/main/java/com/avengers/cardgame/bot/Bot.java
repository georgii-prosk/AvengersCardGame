package com.avengers.cardgame.bot;

import com.avengers.cardgame.messaging.Message;
import org.jetbrains.annotations.NotNull;

/**
 * Бот, умеющий отправлять сообщения.
 */
public interface Bot {

    /**
     * Отправляет сообщение в указанный чат.
     *
     * @param message    сообщение
     * @param chatId идентификатор чата получателя
     */
    void sendMessage(@NotNull Message message, long chatId);
}