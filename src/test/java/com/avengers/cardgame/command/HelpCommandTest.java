package com.avengers.cardgame.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link HelpCommand}.
 */
class HelpCommandTest {

    @Test
    @DisplayName("Возвращает полный текст справки")
    void returnsFullHelpText() {
        Reply reply = new HelpCommand().handle(1L, "Tony", "");

        String expected = """
                /start - запуск бота.
                /help - помощь.
                /profile - информация о твоём профиле в боте.
                /setname <name> - позволяет сменить отображаемое для других пользователей имя. Максимум 128 символов латиницей.
                /setbio <bio> - позволяет обновить короткое описание профиля. Максимум 256 символов латиницей или кириллицей + эмодзи и знаков препинания, пробелов, цифр, спецсимволы (/|\\<>@#$%^&*(){}[]`~;:'"-_=+).
                """;
        Assertions.assertEquals(expected, reply.text());
    }

    @Test
    @DisplayName("Не зависит от данных пользователя")
    void doesNotDependOnUserData() {
        Reply withName = new HelpCommand().handle(1L, "Tony", "");
        Reply withoutName = new HelpCommand().handle(2L, null, "");

        Assertions.assertEquals(withName.text(), withoutName.text());
    }
}