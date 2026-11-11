package com.avengers.cardgame.service;

import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.fakes.FakeConnection;
import com.avengers.cardgame.fakes.FakeConnectionFactory;
import com.avengers.cardgame.fakes.InMemoryHeroRepository;
import com.avengers.cardgame.fakes.InMemoryMarketRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.PurchaseResult;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.service.impl.MarketServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link MarketServiceImpl}.
 */
class MarketServiceTest {

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 1L;

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 2L;

    /** Цена лота, используемая в тестах. */
    private static final int PRICE = 1_000;

    /** Время создания профилей и лотов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Герой, которого выставляют на рынок. */
    private final Hero hero = new Hero(9L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "Genius");

    /** Репозиторий пользователей в памяти. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек в памяти. */
    private InMemoryUserCardRepository userCardRepository;

    /** Репозиторий лотов в памяти. */
    private InMemoryMarketRepository marketRepository;

    /** Фейковое соединение. */
    private FakeConnection connection;

    /** Тестируемый сервис. */
    private MarketService marketService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        marketRepository = new InMemoryMarketRepository();
        InMemoryHeroRepository heroRepository = new InMemoryHeroRepository();
        heroRepository.save(hero);
        connection = new FakeConnection();
        marketService = new MarketServiceImpl(new FakeConnectionFactory(connection), userRepository,
                userCardRepository, marketRepository, heroRepository);
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
     * @param heroId идентификатор героя
     * @return сохранённый лот
     */
    private MarketListing createListingDirectly(long heroId) {
        return marketRepository.saveListing(connection,
                new MarketListing(0, SELLER_ID, heroId, PRICE, CREATED_AT));
    }

    @Test
    @DisplayName("createListing резервирует карточку и фиксирует транзакцию")
    void createListingReservesCardAndCommits() throws Exception {
        saveUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, hero.id());

        MarketListing listing = marketService.createListing(SELLER_ID, hero.id(), 500);

        Assertions.assertEquals(SELLER_ID, listing.sellerId());
        Assertions.assertEquals(hero.id(), listing.heroId());
        Assertions.assertEquals(500, listing.price());
        Assertions.assertTrue(listing.id() > 0);
        Assertions.assertEquals(0, userCardRepository.countOf(SELLER_ID, hero.id()));
        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(1, connection.commitCount());
        Assertions.assertEquals(0, connection.rollbackCount());
    }

    @Test
    @DisplayName("createListing отклоняет цену ниже минимальной")
    void createListingRejectsPriceBelowMinimum() throws Exception {
        saveUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, hero.id());

        Assertions.assertThrows(InvalidPriceException.class,
                () -> marketService.createListing(SELLER_ID, hero.id(), hero.rarity().minPrice() - 1));

        Assertions.assertEquals(1, userCardRepository.countOf(SELLER_ID, hero.id()));
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(0, connection.commitCount());
        Assertions.assertEquals(0, connection.rollbackCount());
    }

    @Test
    @DisplayName("createListing отклоняет цену выше максимальной")
    void createListingRejectsPriceAboveMaximum() throws Exception {
        saveUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, hero.id());

        Assertions.assertThrows(InvalidPriceException.class,
                () -> marketService.createListing(SELLER_ID, hero.id(), hero.rarity().maxPrice() + 1));
    }

    @Test
    @DisplayName("createListing откатывает транзакцию, если карточки нет")
    void createListingWithoutCardRollsBack() throws Exception {
        saveUser(SELLER_ID, 0);

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> marketService.createListing(SELLER_ID, hero.id(), 500));

        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, connection.rollbackCount());
        Assertions.assertEquals(0, connection.commitCount());
    }

    @Test
    @DisplayName("createListing бросает UserNotFoundException для неизвестного продавца")
    void createListingThrowsForUnknownSeller() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> marketService.createListing(SELLER_ID, hero.id(), 500));
    }

    @Test
    @DisplayName("createListing бросает IllegalArgumentException для неизвестного героя")
    void createListingThrowsForUnknownHero() throws Exception {
        saveUser(SELLER_ID, 0);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> marketService.createListing(SELLER_ID, 999L, 500));
    }

    @Test
    @DisplayName("buy передаёт карточку покупателю и удерживает комиссию")
    void buyTransfersCardAndKeepsCommission() throws Exception {
        saveUser(SELLER_ID, 0);
        saveUser(BUYER_ID, PRICE);
        MarketListing listing = createListingDirectly(hero.id());

        PurchaseResult result = marketService.buy(BUYER_ID, listing.id());

        int commission = PRICE * MarketServiceImpl.COMMISSION_PERCENT / 100;
        Assertions.assertEquals(BUYER_ID, result.buyerId());
        Assertions.assertEquals(SELLER_ID, result.sellerId());
        Assertions.assertEquals(hero.id(), result.heroId());
        Assertions.assertEquals(PRICE, result.price());
        Assertions.assertEquals(0, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(PRICE - commission,
                userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(1, userCardRepository.countOf(BUYER_ID, hero.id()));
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, connection.commitCount());
        Assertions.assertEquals(0, connection.rollbackCount());
    }

    @Test
    @DisplayName("Комиссия рынка составляет 5 процентов")
    void commissionIsFivePercent() {
        Assertions.assertEquals(5, MarketServiceImpl.COMMISSION_PERCENT);
        Assertions.assertEquals(50, PRICE * MarketServiceImpl.COMMISSION_PERCENT / 100);
    }

    @Test
    @DisplayName("buy откатывает транзакцию при недостатке средств")
    void buyWithoutFundsRollsBack() throws Exception {
        saveUser(SELLER_ID, 0);
        saveUser(BUYER_ID, PRICE - 1);
        MarketListing listing = createListingDirectly(hero.id());

        Assertions.assertThrows(InsufficientFundsException.class,
                () -> marketService.buy(BUYER_ID, listing.id()));

        Assertions.assertEquals(PRICE - 1, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userCardRepository.countOf(BUYER_ID, hero.id()));
        Assertions.assertEquals(1, connection.rollbackCount());
        Assertions.assertEquals(0, connection.commitCount());
    }

    @Test
    @DisplayName("buy запрещает покупать собственный лот")
    void buyOwnListingIsRejected() throws Exception {
        saveUser(SELLER_ID, PRICE);
        MarketListing listing = createListingDirectly(hero.id());

        ListingUnavailableException exception = Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.buy(SELLER_ID, listing.id()));

        Assertions.assertEquals("cannot buy own listing", exception.reason());
        Assertions.assertEquals(1, connection.rollbackCount());
        Assertions.assertEquals(0, userCardRepository.countOf(SELLER_ID, hero.id()));
    }

    @Test
    @DisplayName("buy откатывает транзакцию для несуществующего лота")
    void buyMissingListingRollsBack() throws Exception {
        saveUser(BUYER_ID, PRICE);

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.buy(BUYER_ID, 999L));

        Assertions.assertEquals(PRICE, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(1, connection.rollbackCount());
    }

    @Test
    @DisplayName("buy бросает UserNotFoundException для неизвестного покупателя")
    void buyThrowsForUnknownBuyer() {
        Assertions.assertThrows(UserNotFoundException.class, () -> marketService.buy(BUYER_ID, 1L));
    }

    @Test
    @DisplayName("cancelListing возвращает карточку продавцу")
    void cancelListingReturnsCard() throws Exception {
        saveUser(SELLER_ID, 0);
        MarketListing listing = createListingDirectly(hero.id());

        MarketListing cancelled = marketService.cancelListing(SELLER_ID, listing.id());

        Assertions.assertEquals(listing, cancelled);
        Assertions.assertEquals(1, userCardRepository.countOf(SELLER_ID, hero.id()));
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, connection.commitCount());
        Assertions.assertEquals(0, connection.rollbackCount());
    }

    @Test
    @DisplayName("cancelListing не позволяет снять чужой лот")
    void cancelListingRejectsAnotherSeller() throws Exception {
        saveUser(SELLER_ID, 0);
        saveUser(BUYER_ID, 0);
        MarketListing listing = createListingDirectly(hero.id());

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.cancelListing(BUYER_ID, listing.id()));

        Assertions.assertEquals(0, userCardRepository.countOf(BUYER_ID, hero.id()));
        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(1, connection.rollbackCount());
    }

    @Test
    @DisplayName("cancelListing бросает UserNotFoundException для неизвестного продавца")
    void cancelListingThrowsForUnknownSeller() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> marketService.cancelListing(SELLER_ID, 1L));
    }

    @Test
    @DisplayName("getActiveListings обогащает лоты именем продавца и героем")
    void getActiveListingsEnrichesItems() throws Exception {
        saveUser(SELLER_ID, 0);
        createListingDirectly(hero.id());

        List<MarketItem> items = marketService.getActiveListings();

        Assertions.assertEquals(1, items.size());
        MarketItem item = items.getFirst();
        Assertions.assertEquals(hero, item.hero());
        Assertions.assertEquals("Seller", item.sellerName());
        Assertions.assertEquals(PRICE, item.listing().price());
    }

    @Test
    @DisplayName("getUserListings возвращает только лоты продавца")
    void getUserListingsFiltersBySeller() throws Exception {
        saveUser(SELLER_ID, 0);
        saveUser(BUYER_ID, 0);
        marketRepository.saveListing(connection,
                new MarketListing(0, SELLER_ID, hero.id(), PRICE, CREATED_AT));
        marketRepository.saveListing(connection,
                new MarketListing(0, BUYER_ID, hero.id(), PRICE, CREATED_AT.plusHours(1)));

        List<MarketItem> items = marketService.getUserListings(SELLER_ID);

        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(SELLER_ID, items.getFirst().listing().sellerId());
    }

    @Test
    @DisplayName("getActiveListings подставляет имя для неизвестного продавца")
    void getActiveListingsUsesPlaceholderSellerName() {
        marketRepository.saveListing(connection,
                new MarketListing(0, 99L, hero.id(), PRICE, CREATED_AT));

        List<MarketItem> items = marketService.getActiveListings();

        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals("Неизвестный игрок", items.getFirst().sellerName());
    }

    @Test
    @DisplayName("getActiveListings пропускает лот с неизвестным героем")
    void getActiveListingsSkipsUnknownHero() {
        marketRepository.saveListing(connection,
                new MarketListing(0, SELLER_ID, 999L, PRICE, CREATED_AT));

        Assertions.assertTrue(marketService.getActiveListings().isEmpty());
    }
}
