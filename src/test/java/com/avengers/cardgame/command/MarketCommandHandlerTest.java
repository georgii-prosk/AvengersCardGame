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
 * Тесты для {@link MarketCommandHandler}.
 */
class MarketCommandHandlerTest {

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
    private MarketCommandHandler handler;

    @BeforeEach
    void setUp() {
        FakeMarketService marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketCommandHandler(marketService, GAME_CONFIG);
    }

    /**
     * Создаёт контекст команды.
     *
     * @return контекст
     */
    private CommandContext context() {
        return new CommandContext(USER_ID, CHAT_ID, "Tony", "", bot);
    }

    /**
     * Проверяет сообщение для пустого рынка.
     */
    @Test
    @DisplayName("Показывает пустой рынок")
    void showsEmptyMarket() {
        handler.handle(context());

        Assertions.assertEquals("Сейчас на рынке нет лотов. Будь первым — выставь карточку через /sell!",
                bot.lastSent().message().text());
    }
}
