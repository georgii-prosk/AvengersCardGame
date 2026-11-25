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

/**
 * Тесты для {@link MarketBuyCommandHandler}.
 */
class MarketBuyCommandHandlerTest {

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 1L;

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 2L;

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
    private MarketBuyCommandHandler handler;

    @BeforeEach
    void setUp() {
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketBuyCommandHandler(marketService, GAME_CONFIG);
    }

    /**
     * Создаёт контекст команды.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return new CommandContext(BUYER_ID, CHAT_ID, "Tony", argument, bot);
    }

    /**
     * Создаёт объявление рынка.
     *
     * @param sellerId идентификатор продавца
     * @return объявление
     */
    private MarketItem item(long sellerId) {
        MarketListing listing = new MarketListing(7L, sellerId, COMMON_HERO.id(), 100, CREATED_AT);
        return new MarketItem(listing, COMMON_HERO, "Seller");
    }

    /**
     * Проверяет показ подтверждения покупки.
     */
    @Test
    @DisplayName("Показывает подтверждение покупки")
    void showsBuyConfirmation() {
        marketService.getListingReturns(item(SELLER_ID));

        handler.handle(context("7"));

        Message message = bot.lastSent().message();
        Assertions.assertEquals("Купить ⚪ Pepper [Обычная] за 100 💎?", message.text());
        Assertions.assertEquals("/market_buy_confirm 7",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет запрет покупки собственного лота.
     */
    @Test
    @DisplayName("Не даёт купить собственный лот")
    void rejectsOwnListing() {
        marketService.getListingReturns(item(BUYER_ID));

        handler.handle(context("7"));

        Assertions.assertEquals("❌ Нельзя купить собственный лот.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет недоступный лот.
     */
    @Test
    @DisplayName("Сообщает о недоступном лоте")
    void reportsUnavailableListing() {
        handler.handle(context("7"));

        Assertions.assertEquals("❌ Этот лот уже продан или снят с продажи.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет некорректный идентификатор лота.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе лота")
    void rejectsInvalidListingId() {
        handler.handle(context("abc"));

        Assertions.assertEquals("Некорректный идентификатор лота.",
                bot.lastSent().message().text());
    }
}
