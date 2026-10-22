package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link HelpCommandHandler}.
 */
class HelpCommandHandlerTest {

    private static final long USER_ID = 1L;
    private static final long CHAT_ID = 10L;

    private FakeBot bot;
    private HelpCommandHandler command;

    @BeforeEach
    void setUp() {
        bot = new FakeBot();
        command = new HelpCommandHandler();
    }

    @Test
    @DisplayName("Отправляет полный текст справки")
    void sendsFullHelpText() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        String expected = """
                /start - запуск бота.
                /help - помощь.
                /profile - информация о твоём профиле в боте.
                /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
                /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
                """;
        Assertions.assertEquals(expected, bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Отправляет ровно одно сообщение")
    void sendsExactlyOneMessage() {
        command.handle(new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot));

        Assertions.assertEquals(1, bot.count());
    }
}