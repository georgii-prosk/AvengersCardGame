package com.avengers.cardgame.command;

import com.avengers.cardgame.service.Message;

/**
 * Команда /start.
 */
public class StartCommand implements CommandHandler {

    /** Текст приветствия. */
    private static final String GREETING = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;

    @Override
    public void handle(CommandContext context) {
        context.bot().sendMessage(new Message(GREETING), context.chatId());
    }
}