package com.avengers.cardgame.command;

/**
 * Команда /start.
 */
public class StartCommand implements Command {

    @Override
    public Reply handle(CommandRequest request) {
        return new Reply("""
                Привет! Это бот AvengersCardGame.
                Тут ты можешь собирать свою коллекцию карточек мстителей,
                обмениваться карточками с другими игроками и устраивать сражения.
                """);
    }
}