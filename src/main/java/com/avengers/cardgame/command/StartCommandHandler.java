package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.BotException;
import com.avengers.cardgame.service.Message;

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
    public void handle(CommandContext context) throws BotException {
        context.bot().sendMessage(new Message(GREETING), context.chatId());
    }
}