package com.avengers.cardgame.command;

/**
 * Команда /start.
 */
public class StartCommand implements Command {

    /** Текст приветствия. */
    private static final String GREETING = """
            Привет! Это бот AvengersCardGame.
            Тут ты можешь собирать свою коллекцию карточек мстителей,
            обмениваться карточками с другими игроками и устраивать сражения.
            """;


    /**
     * Возвращает приветствие.
     *
     * @param request данные запроса
     * @return ответ с приветствием
     */
    @Override
    public Reply handle(CommandRequest request) {
        return new Reply(GREETING);
    }
}