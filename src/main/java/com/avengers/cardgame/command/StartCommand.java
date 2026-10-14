package com.avengers.cardgame.command;

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

    /**
     * Возвращает фиксированное приветственное сообщение.
     *
     * @param userId   идентификатор пользователя
     * @param userName имя пользователя
     * @param argument аргумент команды
     * @return ответ с приветствием
     */
    @Override
    public Reply handle(long userId, String userName, String argument) {
        return new Reply(GREETING);
    }
}