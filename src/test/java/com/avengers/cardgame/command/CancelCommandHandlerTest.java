package com.avengers.cardgame.command;

import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link CancelCommandHandler}.
 */
class CancelCommandHandlerTest {

    /**
     * Проверяет, что команда сообщает об отмене действия.
     */
    @Test
    @DisplayName("Сообщает об отмене действия")
    void sendsCancelMessage() {
        FakeBot bot = new FakeBot();
        CancelCommandHandler handler = new CancelCommandHandler();

        handler.handle(new CommandContext(1L, 10L, "Tony", "", bot));

        Assertions.assertEquals("Действие отменено.", bot.lastSent().message().text());
        Assertions.assertEquals(10L, bot.lastSent().chatId());
    }
}
