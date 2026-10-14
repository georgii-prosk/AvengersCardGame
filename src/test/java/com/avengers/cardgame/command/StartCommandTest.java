package com.avengers.cardgame.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link StartCommand}.
 */
class StartCommandTest {

    @Test
    @DisplayName("Возвращает фиксированное приветствие")
    void returnsFixedGreeting() {
        Reply reply = new StartCommand().handle(1L, "Tony", "");

        String expected = """
                Привет! Это бот AvengersCardGame.
                Тут ты можешь собирать свою коллекцию карточек мстителей,
                обмениваться карточками с другими игроками и устраивать сражения.
                """;
        Assertions.assertEquals(expected, reply.text());
    }

    @Test
    @DisplayName("Не зависит от данных пользователя")
    void doesNotDependOnUserData() {
        Reply withName = new StartCommand().handle(1L, "Tony", "");
        Reply withoutName = new StartCommand().handle(2L, null, "");

        Assertions.assertEquals(withName.text(), withoutName.text());
    }

    @Test
    @DisplayName("Игнорирует аргумент")
    void ignoresArgument() {
        Reply withoutArg = new StartCommand().handle(1L, "Tony", "");
        Reply withArg = new StartCommand().handle(1L, "Tony", "some argument");

        Assertions.assertEquals(withoutArg.text(), withArg.text());
    }
}