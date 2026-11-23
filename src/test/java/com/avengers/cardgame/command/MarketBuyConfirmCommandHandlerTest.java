package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.UserService;
import com.avengers.cardgame.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Тесты для {@link MarketBuyConfirmCommandHandler}.
 */
class MarketBuyConfirmCommandHandlerTest {

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 1L;

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 2L;

    /** Идентификатор чата покупателя. */
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
    private MarketBuyConfirmCommandHandler handler;

    @BeforeEach
    void setUp() throws Exception {
        FakeHeroRepository heroRepository = new FakeHeroRepository();
        heroRepository.save(COMMON_HERO);
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        UserService userService = new UserServiceImpl(userRepository);
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new MarketBuyConfirmCommandHandler(marketService, heroRepository, userService, GAME_CONFIG);
        userRepository.save(User.of(BUYER_ID, PlayerName.of("Tony"), null,
                50, 0, 0, CREATED_AT, CREATED_AT));
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
     * Проверяет успешную покупку и уведомление продавца.
     */
    @Test
    @DisplayName("Покупает лот и уведомляет продавца")
    void buysAndNotifiesSeller() throws Exception {
        marketService.buyReturns(new PurchaseResult(BUYER_ID, SELLER_ID, COMMON_HERO.id(), 100));

        handler.handle(context("7"));

        Message buyerMessage = bot.sent().getFirst().message();
        Assertions.assertTrue(buyerMessage.text().contains("Покупка совершена"));
        Assertions.assertTrue(buyerMessage.text().contains("Pepper"));
        Assertions.assertTrue(buyerMessage.text().contains("Списано: 100 💎"));
        Assertions.assertEquals(CHAT_ID, bot.sent().getFirst().chatId());

        Message sellerMessage = bot.sent().getLast().message();
        Assertions.assertTrue(sellerMessage.text().contains("куплен игроком Tony"));
        Assertions.assertTrue(sellerMessage.text().contains("Тебе начислено 95 💎"));
        Assertions.assertEquals(SELLER_ID, bot.lastSent().chatId());
    }

    /**
     * Проверяет сообщение о нехватке средств.
     */
    @Test
    @DisplayName("Сообщает о нехватке средств")
    void reportsInsufficientFunds() throws Exception {
        marketService.buyThrows(new InsufficientFundsException(100, 30));

        handler.handle(context("7"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Недостаточно средств"));
        Assertions.assertTrue(message.text().contains("Нужно: 100 💎"));
        Assertions.assertTrue(message.text().contains("у тебя: 30 💎"));
    }

    /**
     * Проверяет сообщение при попытке купить свой лот.
     */
    @Test
    @DisplayName("Сообщает о попытке купить свой лот")
    void reportsOwnListingOnConfirm() throws Exception {
        marketService.buyThrows(new OwnListingException(7L));

        handler.handle(context("7"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("собственный лот"));
    }

    /**
     * Проверяет сообщение о недоступном лоте при подтверждении.
     */
    @Test
    @DisplayName("Сообщает о недоступном лоте при подтверждении")
    void reportsUnavailableOnConfirm() throws Exception {
        marketService.buyThrows(new ListingUnavailableException(7L));

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
