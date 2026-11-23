package com.avengers.cardgame.command;

import com.avengers.cardgame.exception.ListingUnavailableException;
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
 * Тесты для {@link MarketCancelCommandHandler}.
 */
class MarketCancelCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Время создания лота для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Фейковый сервис рынка. */
    private FakeMarketService marketService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private MarketCancelCommandHandler handler;

    @BeforeEach
    void setUp() {
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(COMMON_HERO);
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketCancelCommandHandler(marketService, heroRepository);
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
     * Проверяет снятие лота.
     */
    @Test
    @DisplayName("Снимает лот с продажи")
    void cancelsListing() throws Exception {
        marketService.cancelReturns(new MarketListing(7L, USER_ID, COMMON_HERO.id(), 100, CREATED_AT));

        handler.handle(context("7"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Лот #7 снят с продажи"));
        Assertions.assertTrue(message.text().contains("Pepper"));
    }

    /**
     * Проверяет сообщение о недоступном лоте при снятии.
     */
    @Test
    @DisplayName("Сообщает о недоступном лоте при снятии")
    void reportsUnavailableOnCancel() throws Exception {
        marketService.cancelThrows(new ListingUnavailableException(7L));

        handler.handle(context("7"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("уже продан"));
    }

    /**
     * Проверяет некорректный идентификатор лота.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе лота")
    void rejectsInvalidListingId() throws Exception {
        handler.handle(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный идентификатор"));
    }
}
