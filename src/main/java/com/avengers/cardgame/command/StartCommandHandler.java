package com.avengers.cardgame.command;

import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.CommandHandler;

/**
 * Обработчик команды /start.
 */
public class StartCommandHandler implements CommandHandler {

    /** Текст приветствия. */
    private static final String GREETING = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;

    @Override
    public void handle(CommandContext context)  {
        context.bot().sendMessage(new Message(GREETING), context.chatId());
    }
}