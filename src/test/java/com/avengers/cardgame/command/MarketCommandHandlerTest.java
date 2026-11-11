package com.avengers.cardgame.command;

import com.avengers.cardgame.bot.Bot;
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
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.CommandContext;
import com.avengers.cardgame.service.MarketService;
import com.avengers.cardgame.service.impl.MarketServiceImpl;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Тесты для {@link MarketCommandHandler}.
 */
class MarketCommandHandlerTest {

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 1L;

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 2L;

    /** Идентификатор чата покупателя. */
    private static final long CHAT_ID = 20L;

    /** Цена лота, используемая в тестах. */
    private static final int PRICE = 1_000;

    /** Время создания профиля и лота. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Герой, выставляемый на рынок. */
    private final Hero hero = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    /** Игровая конфигурация. */
    private final GameConfig gameConfig = new GameConfig(
            Duration.ofHours(24), Duration.ofMinutes(30), "Кристаллы", "💎");

    /** Фейковый бот. */
    private FakeBot bot;

    /** Репозиторий пользователей в памяти. */
    private InMemoryUserRepository userRepository;

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
        userRepository = new InMemoryUserRepository();
        cardRepository = new InMemoryUserCardRepository();
        heroRepository = new InMemoryHeroRepository();
        heroRepository.save(hero);
        marketRepository = new InMemoryMarketRepository();
        connection = new FakeConnection();
        saveUser(SELLER_ID, 0);
        saveUser(BUYER_ID, PRICE);
        marketService = new MarketServiceImpl(new FakeConnectionFactory(connection), userRepository,
                cardRepository, marketRepository, heroRepository);
    }

    /**
     * Сохраняет пользователя с заданным балансом.
     *
     * @param userId  идентификатор пользователя
     * @param balance баланс валюты
     * @throws Exception если данные пользователя некорректны
     */
    private void saveUser(long userId, int balance) throws Exception {
        userRepository.save(new User(userId, new PlayerName(playerName(userId)), null,
                balance, 0, 0, CREATED_AT, CREATED_AT, null, null));
    }

    /**
     * Подбирает допустимое отображаемое имя для пользователя.
     *
     * @param userId идентификатор пользователя
     * @return имя игрока
     */
    private String playerName(long userId) {
        return userId == SELLER_ID ? "Seller" : "Buyer";
    }

    /**
     * Создаёт лот напрямую в репозитории.
     *
     * @return сохранённый лот
     */
    private MarketListing createListingDirectly() {
        return marketRepository.saveListing(connection,
                new MarketListing(0, SELLER_ID, hero.id(), PRICE, CREATED_AT));
    }

    /**
     * Создаёт обработчик нужного шага.
     *
     * @param step шаг обработки
     * @return обработчик
     */
    private MarketCommandHandler handler(MarketCommandHandler.Step step) {
        return new MarketCommandHandler(marketService, heroRepository, gameConfig, step);
    }

    /**
     * Создаёт контекст покупателя.
     *
     * @param argument аргумент команды
     * @return контекст
     */
    private CommandContext context(String argument) {
        return context(BUYER_ID, argument, bot);
    }

    /**
     * Создаёт контекст указанного пользователя.
     *
     * @param userId   идентификатор пользователя
     * @param argument аргумент команды
     * @param bot      бот
     * @return контекст
     */
    private CommandContext context(long userId, String argument, Bot bot) {
        return new CommandContext(userId, CHAT_ID, playerName(userId), argument, bot);
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
    @DisplayName("START пустого рынка показывает заглушку")
    void startShowsEmptyMarket() throws Exception {
        handler(MarketCommandHandler.Step.START).handle(context(""));

        Assertions.assertEquals("На рынке пока нет лотов. Стань первым продавцом: /sell",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("START показывает лот с кнопкой покупки")
    void startShowsListing() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.START).handle(context(""));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("🏪 Рынок (страница 1/1):"));
        Assertions.assertTrue(message.text().contains("Iron Man"));
        Assertions.assertTrue(message.text().contains("1000 💎"));
        Assertions.assertTrue(callbacks(message).contains("/market_buy " + listing.id()));
    }

    @Test
    @DisplayName("BUY показывает подтверждение покупки")
    void buyShowsConfirmation() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.BUY).handle(context(String.valueOf(listing.id())));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().contains("Подтвердить покупку?"));
        Assertions.assertTrue(message.text().contains("Продавец: Seller"));
        Assertions.assertTrue(callbacks(message).contains("/market_buy_confirm " + listing.id()));
    }

    @Test
    @DisplayName("BUY сообщает о недоступном лоте")
    void buyReportsUnavailableListing() throws Exception {
        handler(MarketCommandHandler.Step.BUY).handle(context("999"));

        Assertions.assertEquals("Этот лот больше недоступен. Обнови рынок: /market",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("BUY отклоняет некорректный идентификатор")
    void buyRejectsInvalidArgument() throws Exception {
        handler(MarketCommandHandler.Step.BUY).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("BUY_CONFIRM покупает лот и уведомляет продавца отдельно")
    void buyConfirmNotifiesSeller() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.BUY_CONFIRM).handle(context(String.valueOf(listing.id())));

        Assertions.assertEquals(2, bot.count());
        Assertions.assertTrue(bot.sent().get(0).message().text().contains("Ты купил Iron Man за 1000 💎."));
        Assertions.assertEquals(CHAT_ID, bot.sent().get(0).chatId());
        Assertions.assertTrue(bot.sent().get(1).message().text().contains("Тебе начислено: 950 💎"));
        Assertions.assertEquals(SELLER_ID, bot.sent().get(1).chatId());
        Assertions.assertEquals(1, cardRepository.countOf(BUYER_ID, hero.id()));
        Assertions.assertEquals(0, marketRepository.size());
    }

    @Test
    @DisplayName("BUY_CONFIRM сообщает о недостатке средств")
    void buyConfirmReportsInsufficientFunds() throws Exception {
        userRepository.spendBalance(connection, BUYER_ID, PRICE);
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.BUY_CONFIRM).handle(context(String.valueOf(listing.id())));

        Assertions.assertEquals("Недостаточно средств: нужно 1000 💎, у тебя 0 💎.",
                bot.lastSent().message().text());
        Assertions.assertEquals(0, cardRepository.countOf(BUYER_ID, hero.id()));
        Assertions.assertEquals(1, connection.rollbackCount());
    }

    @Test
    @DisplayName("BUY_CONFIRM запрещает покупку собственного лота")
    void buyConfirmRejectsOwnListing() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.BUY_CONFIRM)
                .handle(context(SELLER_ID, String.valueOf(listing.id()), bot));

        Assertions.assertEquals("Нельзя купить собственный лот.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("BUY_CONFIRM сообщает о недоступном лоте")
    void buyConfirmReportsUnavailableListing() throws Exception {
        handler(MarketCommandHandler.Step.BUY_CONFIRM).handle(context("999"));

        Assertions.assertEquals("Этот лот больше недоступен. Обнови рынок: /market",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("BUY_CONFIRM отклоняет некорректный идентификатор")
    void buyConfirmRejectsInvalidArgument() throws Exception {
        handler(MarketCommandHandler.Step.BUY_CONFIRM).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("Ошибка уведомления продавца не прерывает покупку")
    void notificationFailureDoesNotBreakPurchase() throws Exception {
        MarketListing listing = createListingDirectly();
        SellerFailingBot failingBot = new SellerFailingBot();

        handler(MarketCommandHandler.Step.BUY_CONFIRM)
                .handle(context(BUYER_ID, String.valueOf(listing.id()), failingBot));

        Assertions.assertEquals(1, failingBot.count());
        Assertions.assertTrue(failingBot.lastSent().message().text().contains("Ты купил Iron Man"));
        Assertions.assertEquals(1, cardRepository.countOf(BUYER_ID, hero.id()));
    }

    @Test
    @DisplayName("MINE показывает собственные лоты")
    void mineShowsOwnListings() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.MINE)
                .handle(context(SELLER_ID, "", bot));

        Message message = bot.lastSent().message();
        Assertions.assertTrue(message.text().startsWith("📦 Твои лоты (страница 1/1):"));
        Assertions.assertTrue(message.text().contains("Iron Man"));
        Assertions.assertTrue(callbacks(message).contains("/market_cancel " + listing.id()));
    }

    @Test
    @DisplayName("MINE без лотов показывает заглушку")
    void mineShowsEmptyMessage() throws Exception {
        handler(MarketCommandHandler.Step.MINE).handle(context(SELLER_ID, "", bot));

        Assertions.assertEquals("У тебя нет активных лотов. Выставь карточку командой /sell.",
                bot.lastSent().message().text());
    }

    @Test
    @DisplayName("MINE_PAGE отклоняет некорректный номер страницы")
    void minePageRejectsInvalidNumber() throws Exception {
        handler(MarketCommandHandler.Step.MINE_PAGE).handle(context(SELLER_ID, "abc", bot));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("CANCEL снимает лот и возвращает карточку")
    void cancelReturnsCard() throws Exception {
        MarketListing listing = createListingDirectly();

        handler(MarketCommandHandler.Step.CANCEL)
                .handle(context(SELLER_ID, String.valueOf(listing.id()), bot));

        Assertions.assertTrue(bot.lastSent().message().text().contains("Лот снят с рынка."));
        Assertions.assertEquals(1, cardRepository.countOf(SELLER_ID, hero.id()));
        Assertions.assertEquals(0, marketRepository.size());
    }

    @Test
    @DisplayName("CANCEL сообщает, если лот не найден")
    void cancelReportsMissingListing() throws Exception {
        handler(MarketCommandHandler.Step.CANCEL).handle(context(SELLER_ID, "999", bot));

        Assertions.assertEquals("Этот лот не найден среди твоих лотов.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("CANCEL отклоняет некорректный идентификатор")
    void cancelRejectsInvalidArgument() throws Exception {
        handler(MarketCommandHandler.Step.CANCEL).handle(context(SELLER_ID, "abc", bot));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    @Test
    @DisplayName("PAGE отклоняет некорректный номер страницы")
    void pageRejectsInvalidNumber() throws Exception {
        handler(MarketCommandHandler.Step.PAGE).handle(context("abc"));

        Assertions.assertEquals("Некорректный формат команды.", bot.lastSent().message().text());
    }

    /**
     * Бот, который не может отправить сообщение продавцу.
     */
    private static final class SellerFailingBot extends FakeBot {

        @Override
        public void sendMessage(@NotNull Message message, long chatId) {
            if (chatId == SELLER_ID) {
                throw new RuntimeException("Telegram is unavailable");
            }
            super.sendMessage(message, chatId);
        }
    }
}
