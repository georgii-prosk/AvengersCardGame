package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link MarketFilterCommandHandler}.
 */
class MarketFilterCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 2L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Время создания лота для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Фейковый сервис рынка. */
    private FakeMarketService marketService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private MarketFilterCommandHandler handler;

    @BeforeEach
    void setUp() {
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketFilterCommandHandler(marketService, GAME_CONFIG);
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
     * Создаёт объявление рынка.
     *
     * @return объявление
     */
    private MarketItem item() {
        MarketListing listing = new MarketListing(7L, SELLER_ID, COMMON_HERO.id(), 100, CREATED_AT);
        return new MarketItem(listing, COMMON_HERO, "Seller");
    }

    /**
     * Проверяет фильтрацию по редкости.
     */
    @Test
    @DisplayName("Применяет фильтр по редкости")
    void appliesRarityFilter() {
        marketService.setActiveListings(List.of(item()));

        handler.handle(context("common"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("#7"));
    }
}
