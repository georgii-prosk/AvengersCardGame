package com.avengers.cardgame.bot;

import com.avengers.cardgame.service.Message;
import org.jetbrains.annotations.NotNull;

/**
 * Бот, умеющий отправлять сообщения.
 */
public interface Bot {

    /**
     * Отправляет сообщение в указанный чат.
     *
     * @param msg    сообщение
     * @param chatId идентификатор чата получателя
     */
    void sendMessage(@NotNull Message msg, long chatId);
}