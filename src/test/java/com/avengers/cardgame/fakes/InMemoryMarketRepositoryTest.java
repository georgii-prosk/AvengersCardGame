package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.InsufficientFundsException;
import com.avengers.cardgame.exception.ListingUnavailableException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.MarketListing;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link InMemoryMarketRepository}.
 */
class InMemoryMarketRepositoryTest {

    /** Идентификатор продавца. */
    private static final long SELLER_ID = 1L;

    /** Идентификатор покупателя. */
    private static final long BUYER_ID = 2L;

    /** Идентификатор героя. */
    private static final long HERO_ID = 10L;

    /** Время создания лота для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository userCardRepository;

    /** Репозиторий рынка. */
    private InMemoryMarketRepository repository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        userCardRepository = new InMemoryUserCardRepository(userRepository);
        repository = new InMemoryMarketRepository(userRepository, userCardRepository);
    }

    /**
     * Создаёт лот без идентификатора.
     *
     * @param sellerId идентификатор продавца
     * @param heroId   идентификатор героя
     * @param price    цена
     * @return новый лот
     */
    private MarketListing listing(long sellerId, long heroId, int price) {
        return new MarketListing(0, sellerId, heroId, price, CREATED_AT);
    }

    /**
     * Регистрирует пользователя с заданным балансом валюты.
     *
     * @param userId  идентификатор пользователя
     * @param balance баланс валюты
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(long userId, int balance) throws Exception {
        userRepository.save(User.of(userId, PlayerName.of(userId == SELLER_ID ? "Seller" : "Buyer"), null,
                balance, 0, 0, CREATED_AT, CREATED_AT));
    }

    /**
     * Проверяет, что сохранение назначает идентификатор.
     */
    @Test
    @DisplayName("Назначает идентификатор при сохранении")
    void assignsIdOnSave() {
        MarketListing first = repository.saveListing(listing(1L, 10L, 100));
        MarketListing second = repository.saveListing(listing(1L, 11L, 100));

        Assertions.assertEquals(1L, first.id());
        Assertions.assertEquals(2L, second.id());
        Assertions.assertEquals(2, repository.size());
    }

    /**
     * Проверяет, что сохранение с готовым идентификатором его сохраняет.
     */
    @Test
    @DisplayName("Сохраняет явный идентификатор")
    void keepsExplicitId() {
        MarketListing saved = repository.saveListing(
                new MarketListing(50L, 1L, 10L, 100, CREATED_AT));

        Assertions.assertEquals(50L, saved.id());
        Assertions.assertTrue(repository.findById(50L).isPresent());
    }

    /**
     * Проверяет поиск по идентификатору.
     */
    @Test
    @DisplayName("Находит лот по идентификатору")
    void findsById() {
        MarketListing saved = repository.saveListing(listing(1L, 10L, 100));

        Assertions.assertTrue(repository.findById(saved.id()).isPresent());
        Assertions.assertTrue(repository.findById(999L).isEmpty());
    }

    /**
     * Проверяет, что активные лоты отсортированы по идентификатору.
     */
    @Test
    @DisplayName("Возвращает активные лоты по порядку")
    void returnsActiveListingsSorted() {
        repository.saveListing(listing(1L, 10L, 100));
        repository.saveListing(listing(2L, 11L, 200));

        List<MarketListing> active = repository.findAllActive();

        Assertions.assertEquals(2, active.size());
        Assertions.assertEquals(1L, active.get(0).id());
        Assertions.assertEquals(2L, active.get(1).id());
    }

    /**
     * Проверяет поиск лотов продавца.
     */
    @Test
    @DisplayName("Находит лоты продавца")
    void findsBySeller() {
        repository.saveListing(listing(1L, 10L, 100));
        repository.saveListing(listing(2L, 11L, 200));
        repository.saveListing(listing(1L, 12L, 300));

        List<MarketListing> listings = repository.findBySeller(1L);

        Assertions.assertEquals(2, listings.size());
        Assertions.assertTrue(listings.stream().allMatch(item -> item.sellerId() == 1L));
    }

    /**
     * Проверяет успешное выставление лота: карточка снимается, лот сохраняется.
     */
    @Test
    @DisplayName("Выставление лота снимает карточку и сохраняет лот")
    void createListingRemovesCardAndSavesListing() throws Exception {
        registerUser(SELLER_ID, 0);
        userCardRepository.addCard(SELLER_ID, HERO_ID);

        MarketListing created = repository.createListing(SELLER_ID, HERO_ID, 100);

        Assertions.assertTrue(created.id() > 0);
        Assertions.assertEquals(0, userCardRepository.countByUserIdAndHeroId(SELLER_ID, HERO_ID));
        Assertions.assertEquals(1, repository.size());
    }

    /**
     * Проверяет, что при отсутствии карточки лот не создаётся.
     */
    @Test
    @DisplayName("Выставление лота без карточки не меняет состояние")
    void createListingKeepsStateWhenNoCard() throws Exception {
        registerUser(SELLER_ID, 0);

        NotEnoughCardsException exception = Assertions.assertThrows(NotEnoughCardsException.class,
                () -> repository.createListing(SELLER_ID, HERO_ID, 100));

        Assertions.assertEquals(HERO_ID, exception.heroId());
        Assertions.assertEquals(1, exception.requested());
        Assertions.assertEquals(0, exception.available());
        Assertions.assertEquals(0, repository.size());
    }

    /**
     * Проверяет успешную покупку: средства списываются, продавцу начисляется сумма, карточка переходит.
     */
    @Test
    @DisplayName("Покупка переводит средства и карточку")
    void buyTransfersFundsAndCard() throws Exception {
        registerUser(BUYER_ID, 150);
        registerUser(SELLER_ID, 0);
        MarketListing saved = repository.saveListing(listing(SELLER_ID, HERO_ID, 100));

        MarketListing deleted = repository.buy(BUYER_ID, saved.id(), 95);

        Assertions.assertEquals(saved.id(), deleted.id());
        Assertions.assertEquals(50, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(95, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(1, userCardRepository.countByUserIdAndHeroId(BUYER_ID, HERO_ID));
        Assertions.assertEquals(0, repository.size());
    }

    /**
     * Проверяет, что при недостатке средств покупка не меняет состояние.
     */
    @Test
    @DisplayName("Покупка при недостатке средств не меняет состояние")
    void buyKeepsStateWhenInsufficientFunds() throws Exception {
        registerUser(BUYER_ID, 30);
        registerUser(SELLER_ID, 0);
        MarketListing saved = repository.saveListing(listing(SELLER_ID, HERO_ID, 100));

        InsufficientFundsException exception = Assertions.assertThrows(InsufficientFundsException.class,
                () -> repository.buy(BUYER_ID, saved.id(), 95));

        Assertions.assertEquals(100, exception.required());
        Assertions.assertEquals(30, exception.available());
        Assertions.assertEquals(30, userRepository.findByUserId(BUYER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userRepository.findByUserId(SELLER_ID).orElseThrow().balance());
        Assertions.assertEquals(0, userCardRepository.countByUserIdAndHeroId(BUYER_ID, HERO_ID));
        Assertions.assertEquals(1, repository.size());
    }

    /**
     * Проверяет, что покупка отсутствующего лота отклоняется.
     */
    @Test
    @DisplayName("Покупка отсутствующего лота отклоняется")
    void buyRejectsMissingListing() throws Exception {
        registerUser(BUYER_ID, 150);

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> repository.buy(BUYER_ID, 999L, 95));
    }

    /**
     * Проверяет, что покупка отсутствующим покупателем возвращает лот на место.
     */
    @Test
    @DisplayName("Покупка отсутствующим покупателем не меняет состояние")
    void buyKeepsStateWhenBuyerMissing() throws Exception {
        registerUser(SELLER_ID, 0);
        MarketListing saved = repository.saveListing(listing(SELLER_ID, HERO_ID, 100));

        Assertions.assertThrows(UserNotFoundException.class,
                () -> repository.buy(999L, saved.id(), 95));

        Assertions.assertEquals(1, repository.size());
        Assertions.assertTrue(repository.findById(saved.id()).isPresent());
    }

    /**
     * Проверяет успешное снятие лота: карточка возвращается продавцу.
     */
    @Test
    @DisplayName("Снятие лота возвращает карточку продавцу")
    void cancelListingReturnsCard() throws Exception {
        registerUser(SELLER_ID, 0);
        MarketListing saved = repository.saveListing(listing(SELLER_ID, HERO_ID, 100));

        MarketListing cancelled = repository.cancelListing(SELLER_ID, saved.id());

        Assertions.assertEquals(saved.id(), cancelled.id());
        Assertions.assertEquals(1, userCardRepository.countByUserIdAndHeroId(SELLER_ID, HERO_ID));
        Assertions.assertEquals(0, repository.size());
    }

    /**
     * Проверяет, что чужой или отсутствующий лот снять нельзя.
     */
    @Test
    @DisplayName("Снятие чужого лота не меняет состояние")
    void cancelListingKeepsStateWhenForeign() throws Exception {
        registerUser(SELLER_ID, 0);
        MarketListing saved = repository.saveListing(listing(SELLER_ID, HERO_ID, 100));

        Assertions.assertThrows(ListingUnavailableException.class,
                () -> repository.cancelListing(BUYER_ID, saved.id()));

        Assertions.assertEquals(1, repository.size());
        Assertions.assertEquals(0, userCardRepository.countByUserIdAndHeroId(BUYER_ID, HERO_ID));
    }
}
