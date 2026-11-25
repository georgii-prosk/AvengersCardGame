package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * Тесты для {@link MarketPageCommandHandler}.
 */
class MarketPageCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new TestGameConfig().defaultConfig();

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private MarketPageCommandHandler handler;

    @BeforeEach
    void setUp() {
        FakeMarketService marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketPageCommandHandler(marketService, GAME_CONFIG);
    }

    /**
     * Создаёт контекст команды.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", argument, bot);
    }

    /**
     * Проверяет некорректный номер страницы.
     */
    @Test
    @DisplayName("Сообщает о некорректной странице")
    void rejectsInvalidPage() {
        handler.handle(context("abc abc"));

        Assertions.assertEquals("Некорректный номер страницы.",
                bot.lastSent().message().text());
    }
}
