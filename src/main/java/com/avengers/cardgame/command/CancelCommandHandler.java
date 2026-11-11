package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /cancel: отменяет текущее действие пользователя.
 */
public class CancelCommandHandler implements CommandHandler {

    /** Текст ответа на отмену. */
    private static final String CANCELLED_MESSAGE = "Действие отменено.";

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(new Message(CANCELLED_MESSAGE), context.chatId());
    }
}
