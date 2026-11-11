package com.avengers.cardgame.service;

import com.avengers.cardgame.dto.PurchaseResult;
import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.InvalidPriceException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.OwnListingException;
import com.avengers.cardgame.fakes.FakeConnectionFactory;
import com.avengers.cardgame.fakes.FakeHeroRepository;
import com.avengers.cardgame.fakes.InMemoryMarketRepository;
import com.avengers.cardgame.fakes.InMemoryUserCardRepository;
import com.avengers.cardgame.fakes.InMemoryUserRepository;
import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.model.HeroRarity;
import com.avengers.cardgame.model.MarketItem;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
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

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 1L;

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 2L;

    /** Обычный герой. */
    private static final Hero COMMON_HERO =
            new Hero(10L, "Pepper", HeroRarity.COMMON, 5, 15, 20, "c");

    /** Эпический герой. */
    private static final Hero EPIC_HERO =
            new Hero(11L, "Iron Man", HeroRarity.EPIC, 80, 90, 50, "e");

    /** Время создания лота для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Репозиторий рынка. */
    private InMemoryMarketRepository marketRepository;

    /** Фабрика фейковых соединений. */
    private FakeConnectionFactory connectionFactory;

    /** Тестируемый сервис. */
    private MarketService marketService;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository();
        FakeHeroRepository heroRepository = new FakeHeroRepository();
        heroRepository.save(COMMON_HERO);
        heroRepository.save(EPIC_HERO);
        marketRepository = new InMemoryMarketRepository();
        connectionFactory = new FakeConnectionFactory();
        marketService = new MarketServiceImpl(connectionFactory, userRepository,
                userCardRepository, marketRepository, heroRepository);
    }

    /**
     * Регистрирует пользователя с заданными балансами.
     *
     * @param userId  идентификатор пользователя
     * @param balance баланс валюты
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(long userId, int balance) throws Exception {
        String name = userId == SELLER_ID ? "Seller" : "Buyer";
        userRepository.save(User.of(userId, PlayerName.of(name), null,
                balance, 0, 0, CREATED_AT, CREATED_AT));
    }

    /**
     * Сохраняет лот напрямую в репозиторий.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена
     * @return сохранённый лот
     */
    private MarketListing saveListing(long sellerId, long heroId, int price) {
        return marketRepository.saveListing(
                new MarketListing(0, sellerId, heroId, price, CREATED_AT));
    }

    /**
     * Проверяет, что выставление лота забирает карточку и сохраняет лот.
     */
    @Test
    @DisplayName("Выставляет лот и убирает карточку из коллекции")
    void createsListingAndRemovesCard() throws Exception {
        registerUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, COMMON_HERO.id());

        MarketListing listing = marketService.createListing(SELLER_ID, COMMON_HERO.id(), 100);

        Assertions.assertTrue(listing.id() > 0);
        Assertions.assertEquals(SELLER_ID, listing.sellerId());
        Assertions.assertEquals(COMMON_HERO.id(), listing.heroId());
        Assertions.assertEquals(100, listing.price());
        Assertions.assertEquals(0, userCardRepository.countByUserId(SELLER_ID));
        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(1, connectionFactory.connection().commitCount());
    }

    /**
     * Проверяет, что цена ниже минимальной отклоняется до транзакции.
     */
    @Test
    @DisplayName("Отклоняет слишком низкую цену")
    void rejectsTooLowPrice() throws Exception {
        registerUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, COMMON_HERO.id());

        Assertions.assertThrows(InvalidPriceException.class,
                () -> marketService.createListing(SELLER_ID, COMMON_HERO.id(),
                        COMMON_HERO.rarity().minPrice() - 1));

        Assertions.assertEquals(1, userCardRepository.countByUserId(SELLER_ID));
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(0, connectionFactory.connection().commitCount());
    }

    /**
     * Проверяет, что цена выше максимальной отклоняется.
     */
    @Test
    @DisplayName("Отклоняет слишком высокую цену")
    void rejectsTooHighPrice() throws Exception {
        registerUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, EPIC_HERO.id());

        Assertions.assertThrows(InvalidPriceException.class,
                () -> marketService.createListing(SELLER_ID, EPIC_HERO.id(),
                        EPIC_HERO.rarity().maxPrice() + 1));
    }

    /**
     * Проверяет, что нельзя выставить карточку, которой нет в коллекции.
     */
    @Test
    @DisplayName("Не выставляет отсутствующую карточку")
    void rejectsListingWithoutCard() throws Exception {
        registerUser(SELLER_ID, 0);

        Assertions.assertThrows(NotEnoughCardsException.class,
                () -> marketService.createListing(SELLER_ID, COMMON_HERO.id(), 100));

        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, connectionFactory.connection().rollbackCount());
    }

    /**
     * Проверяет полный сценарий покупки с комиссией.
     */
    @Test
    @DisplayName("Покупает лот с комиссией 5%")
    void buysListingWithCommission() throws Exception {
        registerUser(BUYER_ID, 150);
        registerUser(SELLER_ID, 0);
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        PurchaseResult result = marketService.buy(BUYER_ID, listing.id());

        Assertions.assertEquals(BUYER_ID, result.buyerId());
        Assertions.assertEquals(SELLER_ID, result.sellerId());
        Assertions.assertEquals(COMMON_HERO.id(), result.heroId());
        Assertions.assertEquals(100, result.price());
        Assertions.assertEquals(50, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(95, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(1, userCardRepository.countByUserId(BUYER_ID));
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, connectionFactory.connection().commitCount());
    }

    /**
     * Проверяет, что нельзя купить собственный лот.
     */
    @Test
    @DisplayName("Не даёт купить собственный лот")
    void rejectsOwnListing() throws Exception {
        registerUser(SELLER_ID, 500);
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        Assertions.assertThrows(OwnListingException.class,
                () -> marketService.buy(SELLER_ID, listing.id()));

        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(500, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет, что покупка отсутствующего лота отклоняется.
     */
    @Test
    @DisplayName("Отклоняет покупку отсутствующего лота")
    void rejectsMissingListing() throws Exception {
        registerUser(BUYER_ID, 500);

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.buy(BUYER_ID, 999L));
    }

    /**
     * Проверяет, что при недостатке средств покупка откатывается.
     */
    @Test
    @DisplayName("Откатывает покупку при недостатке средств")
    void rollsBackPurchaseWhenInsufficientFunds() throws Exception {
        registerUser(BUYER_ID, 30);
        registerUser(SELLER_ID, 0);
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        Assertions.assertThrows(InsufficientFundsException.class,
                () -> marketService.buy(BUYER_ID, listing.id()));

        Assertions.assertEquals(30, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userCardRepository.countByUserId(BUYER_ID));
        Assertions.assertEquals(1, connectionFactory.connection().rollbackCount());
    }

    /**
     * Проверяет, что снятие лота возвращает карточку продавцу.
     */
    @Test
    @DisplayName("Снимает лот и возвращает карточку")
    void cancelsListingAndReturnsCard() throws Exception {
        registerUser(SELLER_ID, 0);
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        MarketListing cancelled = marketService.cancelListing(SELLER_ID, listing.id());

        Assertions.assertEquals(listing.id(), cancelled.id());
        Assertions.assertEquals(0, marketRepository.size());
        Assertions.assertEquals(1, userCardRepository.countByUserId(SELLER_ID));
        Assertions.assertEquals(1, connectionFactory.connection().commitCount());
    }

    /**
     * Проверяет, что чужой лот снять нельзя.
     */
    @Test
    @DisplayName("Не даёт снять чужой лот")
    void rejectsCancellingForeignListing() throws Exception {
        registerUser(SELLER_ID, 0);
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.cancelListing(BUYER_ID, listing.id()));

        Assertions.assertEquals(1, marketRepository.size());
        Assertions.assertEquals(0, userCardRepository.countByUserId(BUYER_ID));
    }

    /**
     * Проверяет, что активные лоты содержат имя продавца.
     */
    @Test
    @DisplayName("Возвращает активные лоты с именем продавца")
    void returnsActiveListingsWithSellerName() throws Exception {
        registerUser(SELLER_ID, 0);
        saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        List<MarketItem> items = marketService.getActiveListings();

        Assertions.assertEquals(1, items.size());
        MarketItem item = items.getFirst();
        Assertions.assertEquals(COMMON_HERO, item.hero());
        Assertions.assertEquals("Seller", item.sellerName());
    }

    /**
     * Проверяет фильтрацию активных лотов по редкости.
     */
    @Test
    @DisplayName("Фильтрует лоты по редкости")
    void filtersActiveListingsByRarity() throws Exception {
        registerUser(SELLER_ID, 0);
        saveListing(SELLER_ID, COMMON_HERO.id(), 100);
        saveListing(SELLER_ID, EPIC_HERO.id(), 500);

        List<MarketItem> common = marketService.getActiveListings(HeroRarity.COMMON);
        List<MarketItem> epic = marketService.getActiveListings(HeroRarity.EPIC);
        List<MarketItem> all = marketService.getActiveListings(null);

        Assertions.assertEquals(1, common.size());
        Assertions.assertEquals(COMMON_HERO, common.getFirst().hero());
        Assertions.assertEquals(1, epic.size());
        Assertions.assertEquals(EPIC_HERO, epic.getFirst().hero());
        Assertions.assertEquals(2, all.size());
    }

    /**
     * Проверяет, что лоты пользователя не смешиваются с чужими.
     */
    @Test
    @DisplayName("Возвращает только лоты указанного продавца")
    void returnsOnlySellerListings() throws Exception {
        registerUser(BUYER_ID, 0);
        registerUser(SELLER_ID, 0);
        saveListing(SELLER_ID, COMMON_HERO.id(), 100);
        saveListing(BUYER_ID, EPIC_HERO.id(), 500);

        List<MarketItem> items = marketService.getUserListings(SELLER_ID);

        Assertions.assertEquals(1, items.size());
        Assertions.assertEquals(SELLER_ID, items.getFirst().listing().sellerId());
    }

    /**
     * Проверяет получение одного лота по идентификатору.
     */
    @Test
    @DisplayName("Возвращает лот по идентификатору")
    void returnsListingById() throws Exception {
        MarketListing listing = saveListing(SELLER_ID, COMMON_HERO.id(), 100);

        MarketItem item = marketService.getListing(listing.id());

        Assertions.assertEquals(listing, item.listing());
        Assertions.assertEquals(COMMON_HERO, item.hero());
    }

    /**
     * Проверяет, что отсутствующий лот недоступен.
     */
    @Test
    @DisplayName("Бросает исключение для отсутствующего лота")
    void throwsForMissingListing() {
        Assertions.assertThrows(ListingUnavailableException.class,
                () -> marketService.getListing(999L));
    }
}
