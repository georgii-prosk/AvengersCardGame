package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.model.UserCard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Тесты для {@link InMemoryUserCardRepository}.
 */
class InMemoryUserCardRepositoryTest {

    /** Идентификатор пользователя для тестов. */
    private static final long USER_ID = 1L;

    /** Идентификатор отсутствующего пользователя. */
    private static final long MISSING_USER_ID = 999L;

    /** Идентификатор героя для тестов. */
    private static final long HERO_ID = 2L;

    /** Время создания профиля для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository userRepository;

    /** Репозиторий карточек. */
    private InMemoryUserCardRepository repository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        repository = new InMemoryUserCardRepository(userRepository);
    }

    /**
     * Регистрирует пользователя с заданным балансом кусочков.
     *
     * @param shards баланс кусочков
     * @throws Exception при ошибке сохранения
     */
    private void registerUser(int shards) throws Exception {
        userRepository.save(User.of(USER_ID, PlayerName.of("Tony"), null,
                0, shards, 0, CREATED_AT, CREATED_AT));
    }

    /**
     * Проверяет, что повторное добавление увеличивает количество.
     */
    @Test
    @DisplayName("Суммирует повторные добавления")
    void sumsRepeatedAdditions() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        List<UserCard> cards = repository.findByUserId(USER_ID);

        Assertions.assertEquals(1, cards.size());
        Assertions.assertEquals(3, cards.getFirst().count());
        Assertions.assertEquals(3, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет частичное удаление карточек.
     */
    @Test
    @DisplayName("Удаляет часть карточек")
    void removesPartOfCards() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 2);

        Assertions.assertTrue(removed);
        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет, что при удалении всех карточек запись исчезает.
     */
    @Test
    @DisplayName("Удаляет запись при полном удалении")
    void removesEntryWhenAllCardsRemoved() {
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 1);

        Assertions.assertTrue(removed);
        Assertions.assertTrue(repository.findByUserId(USER_ID).isEmpty());
    }

    /**
     * Проверяет, что нельзя удалить больше карточек, чем есть.
     */
    @Test
    @DisplayName("Не удаляет больше, чем есть")
    void doesNotRemoveMoreThanAvailable() {
        repository.addCard(USER_ID, HERO_ID);

        boolean removed = repository.removeCard(USER_ID, HERO_ID, 2);

        Assertions.assertFalse(removed);
        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
    }

    /**
     * Проверяет, что удаление отсутствующей карточки возвращает {@code false}.
     */
    @Test
    @DisplayName("Не удаляет отсутствующую карточку")
    void doesNotRemoveMissingCard() {
        Assertions.assertFalse(repository.removeCard(USER_ID, HERO_ID, 1));
    }

    /**
     * Проверяет, что неположительное количество для удаления отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительном количестве")
    void throwsOnNonPositiveCount() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.removeCard(USER_ID, HERO_ID, 0));
    }

    /**
     * Проверяет, что карточки разных пользователей не смешиваются.
     */
    @Test
    @DisplayName("Разделяет карточки пользователей")
    void separatesUsersCards() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(2L, HERO_ID);

        Assertions.assertEquals(1, repository.countByUserId(USER_ID));
        Assertions.assertEquals(1, repository.countByUserId(2L));
    }

    /**
     * Проверяет, что подсчёт ведётся по конкретной карточке.
     */
    @Test
    @DisplayName("Считает копии конкретной карточки")
    void countsCardsByHeroId() {
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, 3L);

        Assertions.assertEquals(2, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(USER_ID, 3L));
    }

    /**
     * Проверяет, что для отсутствующей карточки возвращается ноль.
     */
    @Test
    @DisplayName("Возвращает ноль для отсутствующей карточки")
    void returnsZeroForMissingCard() {
        repository.addCard(USER_ID, HERO_ID);

        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(USER_ID, 999L));
        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(999L, HERO_ID));
    }

    /**
     * Проверяет успешный разбор: карточки списываются, кусочки начисляются.
     */
    @Test
    @DisplayName("Разбор списывает карточки и начисляет кусочки")
    void disassembleRemovesCardsAndAddsShards() throws Exception {
        registerUser(5);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);
        repository.addCard(USER_ID, HERO_ID);

        int balance = repository.disassemble(USER_ID, HERO_ID, 2, 16);

        Assertions.assertEquals(21, balance);
        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(21, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что при нехватке карточек разбор не меняет состояние.
     */
    @Test
    @DisplayName("Разбор при нехватке карточек не меняет состояние")
    void disassembleKeepsStateWhenNotEnoughCards() throws Exception {
        registerUser(5);
        repository.addCard(USER_ID, HERO_ID);

        NotEnoughCardsException exception = Assertions.assertThrows(NotEnoughCardsException.class,
                () -> repository.disassemble(USER_ID, HERO_ID, 2, 16));

        Assertions.assertEquals(HERO_ID, exception.heroId());
        Assertions.assertEquals(2, exception.requested());
        Assertions.assertEquals(1, exception.available());
        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(5, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что разбор отсутствующего пользователя откатывает списание карточек.
     */
    @Test
    @DisplayName("Разбор отсутствующего пользователя не меняет состояние")
    void disassembleKeepsStateWhenUserMissing() {
        repository.addCard(MISSING_USER_ID, HERO_ID);

        Assertions.assertThrows(UserNotFoundException.class,
                () -> repository.disassemble(MISSING_USER_ID, HERO_ID, 1, 8));

        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(MISSING_USER_ID, HERO_ID));
    }

    /**
     * Проверяет успешный крафт: кусочки списываются, карточка добавляется.
     */
    @Test
    @DisplayName("Крафт списывает кусочки и добавляет карточку")
    void craftSpendsShardsAndAddsCard() throws Exception {
        registerUser(20);

        int balance = repository.craft(USER_ID, HERO_ID, 5);

        Assertions.assertEquals(15, balance);
        Assertions.assertEquals(1, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(15, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что при нехватке кусочков крафт не меняет состояние.
     */
    @Test
    @DisplayName("Крафт при нехватке кусочков не меняет состояние")
    void craftKeepsStateWhenNotEnoughShards() throws Exception {
        registerUser(2);

        InsufficientShardsException exception = Assertions.assertThrows(InsufficientShardsException.class,
                () -> repository.craft(USER_ID, HERO_ID, 5));

        Assertions.assertEquals(5, exception.required());
        Assertions.assertEquals(2, exception.available());
        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(USER_ID, HERO_ID));
        Assertions.assertEquals(2, userRepository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что крафт отсутствующего пользователя не добавляет карточку.
     */
    @Test
    @DisplayName("Крафт отсутствующего пользователя не меняет состояние")
    void craftKeepsStateWhenUserMissing() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> repository.craft(MISSING_USER_ID, HERO_ID, 5));

        Assertions.assertEquals(0, repository.countByUserIdAndHeroId(MISSING_USER_ID, HERO_ID));
    }
}
