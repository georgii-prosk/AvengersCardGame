package com.avengers.cardgame.command;

import com.avengers.cardgame.config.GameConfig;
import com.avengers.cardgame.fakes.FakeBot;
import com.avengers.cardgame.fakes.FakeConnection;
import com.avengers.cardgame.fakes.FakeConnectionFactory;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryMarketRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.messaging.Button;
import com.avengers.cardgame.messaging.Message;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.impl.MarketServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link SellCommandHandler}.
 */
class SellCommandHandlerTest {

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 1L;

    /** Идентификатор чата. */
    private static final long CHAT_ID = 10L;

    /** Время создания профиля. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Обычный герой, которым владеет продавец. */
    private final Hero common = new Hero(5L, "Pepper Potts", HeroRarity.COMMON, 5, 15, 20, "Assistant");

    /** Эпический герой, которым продавец не владеет. */
    private final Hero epic = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    /** Игровая конфигурация. */
    private final GameConfig gameConfig = new GameConfig(
            Duration.ofHours(24), Duration.ofMinutes(30), "Кристаллы", "💎");

    /** Фейковый бот. */
    private FakeBot bot;

    /** Репозиторий карточек в памяти. */
    private InMemoryUserCardRepository cardRepository;

    /** Репозиторий героев в памяти. */
    private InMemoryHeroRepository heroRepository;

    /** Репозиторий лотов в памяти. */
    private InMemoryMarketRepository marketRepository;

    /** Фейковое соединение. */
    private FakeConnection connection;

    /** Тестируемый сервис рынка. */
    private MarketService marketService;

    @BeforeEach
    void setUp() throws Exception {
        bot = new FakeBot();
        InMemoryUserRepository userRepository = new InMemoryUserRepository();
        cardRepository = new InMemoryUserCardRepository();
        heroRepository = new InMemoryHeroRepository();
        heroRepository.save(common);
        heroRepository.save(epic);
        marketRepository = new InMemoryMarketRepository();
        connection = new FakeConnection();
        userRepository.save(new User(SELLER_ID, new PlayerName("Tony"), null,
                0, 0, 0, CREATED_AT, CREATED_AT, null, null));
        cardRepository.addCard(SELLER_ID, common.id());
        marketService = new MarketServiceImpl(new FakeConnectionFactory(connection), userRepository,
                cardRepository, marketRepository, heroRepository);
    }

    /**
     * Создаёт обработчик нужного шага.
     *
     * @param step шаг обработки
     * @return обработчик
     */
    private SellCommandHandler handler(SellCommandHandler.Step step) {
        return new SellCommandHandler(marketService, cardRepository, heroRepository, gameConfig, step);
    }

    /**
     * Создаёт контекст команды.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return new CommandContext(SELLER_ID, CHAT_ID, "Tony", argument, bot);
    }

    /**
     * Собирает callback-данные всех кнопок сообщения.
     *
     * @param message сообщение
     * @return список callback-данных
     */
    private List<String> callbacks(Message message) {
        List<String> callbacks = new ArrayList<>();
        for (List<Button> row : message.buttons()) {
            for (Button button : row) {
                callbacks.add(button.callbackData());
            }
        }
        return callbacks;
    }

    @Test
    @DisplayName("START показывает коллекцию с кнопками выбора")
    void startShowsInventory() throws Exception {
        handler(SellCommandHandler.Step.START).handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("💰 Выбери карточку для продажи (страница 1/1):"));
        Assertions.assertTrue(message.text().contains("Pepper Potts"));
        Assertions.assertTrue(callbacks(message).contains("/sell_pick 5"));
    }

    @Test
    @DisplayName("PICK показывает допустимый диапазон цен и варианты")
    void pickShowsPriceOptions() throws Exception {
        handler(SellCommandHandler.Step.PICK).handle(context("5"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Допустимая цена: 10–200 💎"));
        List<String> callbacks = callbacks(message);
        Assertions.assertTrue(callbacks.contains("/sell_price 5 10"));
        Assertions.assertTrue(callbacks.contains("/sell_price 5 105"));
        Assertions.assertTrue(callbacks.contains("/sell_price 5 200"));
        Assertions.assertTrue(callbacks.contains("/cancel"));
    }

    @Test
    @DisplayName("PICK отклоняет карточку, которой нет в коллекции")
    void pickRejectsNotOwnedCard() throws Exception {
        handler(SellCommandHandler.Step.PICK).handle(context("9"));

        Assertions.assertEquals("У тебя нет этой карточки.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PICK отклоняет неизвестного героя")
    void pickRejectsUnknownHero() throws Exception {
        handler(SellCommandHandler.Step.PICK).handle(context("999"));

        Assertions.assertEquals("Такой карточки не существует.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PICK отклоняет некорректный аргумент")
    void pickRejectsInvalidArgument() throws Exception {
        handler(SellCommandHandler.Step.PICK).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PRICE показывает подтверждение с комиссией")
    void priceShowsConfirmation() throws Exception {
        handler(SellCommandHandler.Step.PRICE).handle(context("5 100"));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("за 100 💎"));
        Assertions.assertTrue(message.text().contains("Комиссия рынка при продаже: 5%."));
        Assertions.assertTrue(callbacks(message).contains("/sell_confirm 5 100"));
    }

    @Test
    @DisplayName("PRICE отклоняет нулевую цену и некорректный формат")
    void priceRejectsInvalidArguments() throws Exception {
        handler(SellCommandHandler.Step.PRICE).handle(context("5 0"));
        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());

        handler(SellCommandHandler.Step.PRICE).handle(context("5"));
        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PRICE отклоняет неизвестного героя")
    void priceRejectsUnknownHero() throws Exception {
        handler(SellCommandHandler.Step.PRICE).handle(context("999 100"));

        Assertions.assertEquals("Такой карточки не существует.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("CONFIRM выставляет лот и забирает карточку")
    void confirmCreatesListing() throws Exception {
        handler(SellCommandHandler.Step.CONFIRM).handle(context("5 100"));

        Assertions.assertTrue(bot.lastSent().message().text()
                .contains("Лот выставлен: Pepper Potts за 100 💎"));
        Assertions.assertEquals(0, cardRepository.countOf(SELLER_ID, common.id()));
        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(1, connection.commitCount());
    }

    @Test
    @DisplayName("CONFIRM сообщает о недопустимой цене")
    void confirmReportsInvalidPrice() throws Exception {
        handler(SellCommandHandler.Step.CONFIRM).handle(context("5 5"));

        Assertions.assertEquals("Недопустимая цена. Для этой редкости: 10–200 💎.",
                bot.lastSent().message().text());
        Assertions.assertEquals(1, cardRepository.countOf(SELLER_ID, common.id()));
    }

    @Test
    @DisplayName("CONFIRM сообщает об отсутствии карточки")
    void confirmReportsMissingCard() throws Exception {
        cardRepository.removeCard(connection, SELLER_ID, common.id());

        handler(SellCommandHandler.Step.CONFIRM).handle(context("5 100"));

        Assertions.assertEquals("У тебя нет этой карточки.", bot.lastSent().message().text());
        Assertions.assertEquals(0, marketRepository.size());
    }

    @Test
    @DisplayName("CONFIRM отклоняет некорректный формат")
    void confirmRejectsInvalidFormat() throws Exception {
        handler(SellCommandHandler.Step.CONFIRM).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PAGE отклоняет некорректный номер страницы")
    void pageRejectsInvalidNumber() throws Exception {
        handler(SellCommandHandler.Step.PAGE).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }
}
