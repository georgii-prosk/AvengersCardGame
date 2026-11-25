package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.config.TestGameConfig;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link SellConfirmCommandHandler}.
 */
class SellConfirmCommandHandlerTest {

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

    /** Фейковый сервис рынка. */
    private FakeMarketService marketService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellConfirmCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(COMMON_HERO);
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new SellConfirmCommandHandler(marketService, heroRepository, GAME_CONFIG);
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
     * Проверяет успешное создание лота.
     */
    @Test
    @DisplayName("Сообщает о созданном лоте")
    void createsListing() throws Exception {
        marketService.createReturns(new MarketListing(7L, USER_ID, COMMON_HERO.id(),
                100, LocalDateTime.now()));

        handler.handle(context("1 100"));

        Message message = bot.lastSent().message();
        Assertions.assertEquals("✅ Лот #7 создан: ⚪ Pepper [Обычная] за 100 💎.",
                message.text());
    }

    /**
     * Проверяет обработку отсутствующей карточки.
     */
    @Test
    @DisplayName("Обрабатывает отсутствие карточки")
    void handlesNotEnoughCards() throws Exception {
        marketService.createThrows(new NotEnoughCardsException(1L, 1, 0));

        handler.handle(context("1 100"));

        Assertions.assertEquals("❌ У тебя нет этой карточки в коллекции.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет обработку недопустимой цены.
     */
    @Test
    @DisplayName("Обрабатывает недопустимую цену")
    void handlesInvalidPrice() throws Exception {
        marketService.createThrows(new InvalidPriceException(5, 10, 200));

        handler.handle(context("1 5"));

        Assertions.assertEquals("❌ Цена должна быть от 10 до 200 💎.",
                bot.lastSent().message().text());
    }

    /**
     * Проверяет некорректные параметры подтверждения.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры лота")
    void rejectsInvalidListingArguments() throws Exception {
        handler.handle(context("1"));

        Assertions.assertEquals("Некорректные параметры лота.",
                bot.lastSent().message().text());
    }
}
