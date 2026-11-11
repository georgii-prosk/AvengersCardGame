package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /cancel: подтверждает отмену текущего действия.
 */
public class CancelCommandHandler implements CommandHandler {

    /** Текст ответа при отмене действия. */
    private static final String CANCEL_MESSAGE = "Действие отменено.";

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(new Message(CANCEL_MESSAGE), context.chatId());
    }
}
