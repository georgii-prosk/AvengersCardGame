package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeMarketService;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.service.CommandContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Тесты для {@link SellCommandHandler}.
 */
class SellCommandHandlerTest {

    /** Идентификатор пользователя. */
    private static final long USER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(1L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Игровая конфигурация для тестов. */
    private static final GameConfig GAME_CONFIG =
            new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository cardRepository;

    /** Фейковый сервис рынка. */
    private FakeMarketService marketService;

    /** Фейковый бот. */
    private FakeBot bot;

    /** Тестируемый обработчик. */
    private SellCommandHandler handler;

    @BeforeEach
    void setUp() {
        cardRepository = new InMemoryUserCardRepository();
        FakeHeroRepository heroRepository = new FakeHeroRepository();
        heroRepository.save(COMMON_HERO);
        marketService = new FakeMarketService();
        bot = new FakeBot();
        handler = new SellCommandHandler(cardRepository, heroRepository, marketService, GAME_CONFIG);
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
     * Проверяет пустое сообщение при отсутствии карточек.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточек")
    void sendsEmptyMessage() {
        handler.handle(context(""));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет карточек"));
    }

    /**
     * Проверяет отображение коллекции для продажи.
     */
    @Test
    @DisplayName("Показывает карточки для продажи")
    void rendersSelection() {
        cardRepository.addCard(USER_ID, COMMON_HERO.id());

        handler.handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("страница 1/1"));
        Assertions.assertEquals("/sell_pick 1",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет некорректный номер страницы.
     */
    @Test
    @DisplayName("Сообщает о некорректной странице")
    void rejectsInvalidPage() {
        handler.handlePage(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный номер"));
    }

    /**
     * Проверяет показ вариантов цены.
     */
    @Test
    @DisplayName("Показывает варианты цены")
    void showsPriceOptions() {
        cardRepository.addCard(USER_ID, COMMON_HERO.id());

        handler.handlePick(context("1"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("от 10 до 200"));
        Assertions.assertEquals(3, message.buttons().getFirst().size());
        Assertions.assertEquals("/sell_price 1 10",
                message.buttons().getFirst().getFirst().callbackData());
        Assertions.assertEquals("/sell_price 1 105",
                message.buttons().getFirst().get(1).callbackData());
        Assertions.assertEquals("/sell_price 1 200",
                message.buttons().getFirst().getLast().callbackData());
    }

    /**
     * Проверяет некорректный идентификатор карточки.
     */
    @Test
    @DisplayName("Сообщает о некорректном идентификаторе")
    void rejectsInvalidHeroId() {
        handler.handlePick(context("abc"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректный идентификатор"));
    }

    /**
     * Проверяет отсутствующего героя.
     */
    @Test
    @DisplayName("Сообщает об отсутствующем герое")
    void rejectsUnknownHero() {
        handler.handlePick(context("999"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("не существует"));
    }

    /**
     * Проверяет карточку, которой нет в коллекции.
     */
    @Test
    @DisplayName("Сообщает об отсутствии карточки в коллекции")
    void rejectsMissingCard() {
        handler.handlePick(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет в твоей коллекции"));
    }

    /**
     * Проверяет подтверждение лота с расчётом комиссии.
     */
    @Test
    @DisplayName("Показывает подтверждение с комиссией")
    void showsConfirmationWithCommission() {
        handler.handlePrice(context("1 100"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Комиссия при продаже: 5%"));
        Assertions.assertTrue(message.text().contains("Ты получишь 95"));
        Assertions.assertEquals("/sell_confirm 1 100",
                message.buttons().getFirst().getFirst().callbackData());
    }

    /**
     * Проверяет цену вне допустимого диапазона.
     */
    @Test
    @DisplayName("Отклоняет цену вне диапазона")
    void rejectsPriceOutOfRange() {
        handler.handlePrice(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("от 10 до 200"));
    }

    /**
     * Проверяет некорректные параметры цены.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры цены")
    void rejectsInvalidPriceArguments() {
        handler.handlePrice(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректные параметры"));
    }

    /**
     * Проверяет успешное создание лота.
     */
    @Test
    @DisplayName("Сообщает о созданном лоте")
    void createsListing() throws Exception {
        marketService.createReturns(new MarketListing(7L, USER_ID, COMMON_HERO.id(),
                100, LocalDateTime.now()));

        handler.handleConfirm(context("1 100"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Лот #7 создан"));
        Assertions.assertTrue(message.text().contains("Pepper"));
        Assertions.assertTrue(message.text().contains("100 💎"));
    }

    /**
     * Проверяет обработку отсутствующей карточки.
     */
    @Test
    @DisplayName("Обрабатывает отсутствие карточки")
    void handlesNotEnoughCards() throws Exception {
        marketService.createThrows(new NotEnoughCardsException(1L, 1, 0));

        handler.handleConfirm(context("1 100"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("нет этой карточки"));
    }

    /**
     * Проверяет обработку недопустимой цены.
     */
    @Test
    @DisplayName("Обрабатывает недопустимую цену")
    void handlesInvalidPrice() throws Exception {
        marketService.createThrows(new InvalidPriceException(5, 10, 200));

        handler.handleConfirm(context("1 5"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("от 10 до 200"));
    }

    /**
     * Проверяет некорректные параметры подтверждения.
     */
    @Test
    @DisplayName("Отклоняет некорректные параметры лота")
    void rejectsInvalidListingArguments() throws Exception {
        handler.handleConfirm(context("1"));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Некорректные параметры"));
    }
}
