package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link MarketMineCommandHandler}.
 */
class MarketMineCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new TestGameConfig().defaultConfig();

    /** Время создания лота для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Фейковый сервис рынка. */
    private FakeMarketService marketService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private MarketMineCommandHandler handler;

    @BeforeEach
    void setUp() {
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketMineCommandHandler(marketService, GAME_CONFIG);
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
     * Создаёт объявление рынка.
     *
     * @return объявление
     */
    private MarketItem item() {
        MarketListing listing = new MarketListing(7L, USER_ID, COMMON_HERO.id(), 100, CREATED_AT);
        return new MarketItem(listing, COMMON_HERO, "Seller");
    }

    /**
     * Проверяет пустой список собственных лотов.
     */
    @Test
    @DisplayName("Сообщает об отсутствии собственных лотов")
    void showsEmptyUserListings() {
        handler.handle(context());

        Assertions.assertEquals("📋 У тебя нет активных лотов.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет список собственных лотов.
     */
    @Test
    @DisplayName("Показывает собственные лоты")
    void showsUserListings() {
        marketService.setUserListings(List.of(item()));

        handler.handle(context());

        Message message = bot.lastSent().message();
        Assertions.assertEquals("""
                📋 Твои лоты:

                #7 ⚪ Pepper [Обычная] — 100 💎""", message.text());
        Assertions.assertEquals("/market_cancel 7",
                message.buttons().getFirst().getFirst().callbackData());
    }
}
