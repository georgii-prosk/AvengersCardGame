package com.avengers.cardgame.command;

import com.avengers.cardgame.service.Message;

/**
 * Команда /start.
 */
public class StartCommand extends SafeCommandHandler {

    /** Текст приветствия. */
    private static final String GREETING = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;

    /**
     * Отправляет приветственное сообщение.
     *
     * @param context контекст выполнения команды
     */
    @Override
    protected void doHandle(CommandContext context) {
        context.bot().sendMessage(new Message(GREETING), context.chatId());
    }
}