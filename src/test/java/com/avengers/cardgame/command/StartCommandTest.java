package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link StartCommand}.
 */
class StartCommandTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeBot bot;
    private StartCommand command;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        command = new StartCommand();
    }

    @Test
    @DisplayName("Отправляет фиксированное приветствие")
    void sendsFixedGreeting() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
                Привет! Это бот AvengersCardGame.
                Тут ты можешь собирать свою коллекцию карточек мстителей,
                обмениваться карточками с другими игроками и устраивать сражения.
                """;
        Assertions.assertEquals(expected, bot.lastText());
    }

    @Test
    @DisplayName("Отправляет сообщение в правильный чат")
    void sendsToCorrectChat() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());
    }

    @Test
    @DisplayName("Отправляет ровно одно сообщение")
    void sendsExactlyOneMessage() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals(1, bot.count());
    }

    @Test
    @DisplayName("Не зависит от данных пользователя")
    void doesNotDependOnUserData() {
        FakeBot bot1 = new FakeBot();
        FakeBot bot2 = new FakeBot();

        command.handle(new CommandContext(1L, CHAT_ID, "Tony", "", bot1));
        command.handle(new CommandContext(2L, CHAT_ID, null, "", bot2));

        Assertions.assertEquals(bot1.lastText(), bot2.lastText());
    }
}