package com.avengers.cardgame.command;

import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /cancel: отменяет текущее действие без ответного сообщения.
 */
public class CancelCommandHandler implements CommandHandler {

    @Override
    public void handle(CommandContext context) {
    }
}
