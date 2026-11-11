package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link InMemoryUserRepository}: операции с балансом и кусочками.
 */
class InMemoryUserRepositoryTest {

    /** Идентификатор пользователя, используемый в тестах. */
    private static final long USER_ID = 1L;

    /** Время создания профиля. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Фейковый репозиторий. */
    private InMemoryUserRepository repository;

    /** Фейковое соединение. */
    private FakeConnection connection;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        connection = new FakeConnection();
    }

    /**
     * Сохраняет пользователя с заданными балансом и кусочками.
     *
     * @param balance баланс валюты
     * @param shards  количество кусочков
     * @throws UserAlreadyExistsException если пользователь уже сохранён
     */
    private void saveUser(int balance, int shards) throws UserAlreadyExistsException {
        repository.save(new User(USER_ID, new PlayerName("Tony"), null, balance, shards, 0,
                CREATED_AT, CREATED_AT, null, null));
    }

    @Test
    @DisplayName("Находит сохранённого пользователя")
    void findsSavedUser() throws Exception {
        saveUser(10, 3);

        User found = repository.findByUserId(USER_ID).orElseThrow();

        Assertions.assertEquals(10, found.balance());
        Assertions.assertEquals(3, found.shards());
    }

    @Test
    @DisplayName("Возвращает пустой результат для неизвестного пользователя")
    void returnsEmptyForUnknownUser() {
        Assertions.assertTrue(repository.findByUserId(USER_ID).isEmpty());
    }

    @Test
    @DisplayName("Повторное сохранение бросает UserAlreadyExistsException")
    void duplicateSaveThrows() throws Exception {
        saveUser(10, 0);

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> saveUser(20, 0));
    }

    @Test
    @DisplayName("spendBalance уменьшает баланс при достатке средств")
    void spendBalanceDecreasesBalance() throws Exception {
        saveUser(100, 0);

        boolean result = repository.spendBalance(connection, USER_ID, 40);

        Assertions.assertTrue(result);
        Assertions.assertEquals(60, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    @Test
    @DisplayName("spendBalance возвращает false при недостатке средств")
    void spendBalanceReturnsFalseWhenInsufficient() throws Exception {
        saveUser(10, 0);

        boolean result = repository.spendBalance(connection, USER_ID, 11);

        Assertions.assertFalse(result);
        Assertions.assertEquals(10, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    @Test
    @DisplayName("spendBalance возвращает false для неизвестного пользователя")
    void spendBalanceReturnsFalseForUnknownUser() {
        Assertions.assertFalse(repository.spendBalance(connection, USER_ID, 1));
    }

    @Test
    @DisplayName("spendShards уменьшает кусочки при достатке")
    void spendShardsDecreasesShards() throws Exception {
        saveUser(0, 100);

        boolean result = repository.spendShards(connection, USER_ID, 40);

        Assertions.assertTrue(result);
        Assertions.assertEquals(60, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("spendShards возвращает false при недостатке кусочков")
    void spendShardsReturnsFalseWhenInsufficient() throws Exception {
        saveUser(0, 5);

        boolean result = repository.spendShards(connection, USER_ID, 6);

        Assertions.assertFalse(result);
        Assertions.assertEquals(5, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("Не даёт уйти балансу в минус при точном списании")
    void spendBalanceCanReachZero() throws Exception {
        saveUser(50, 0);

        Assertions.assertTrue(repository.spendBalance(connection, USER_ID, 50));
        Assertions.assertEquals(0, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    @Test
    @DisplayName("addBalance увеличивает баланс")
    void addBalanceIncreasesBalance() throws Exception {
        saveUser(10, 0);

        boolean result = repository.addBalance(connection, USER_ID, 15);

        Assertions.assertTrue(result);
        Assertions.assertEquals(25, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    @Test
    @DisplayName("addShards увеличивает кусочки")
    void addShardsIncreasesShards() throws Exception {
        saveUser(0, 1);

        boolean result = repository.addShards(connection, USER_ID, 20);

        Assertions.assertTrue(result);
        Assertions.assertEquals(21, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    @Test
    @DisplayName("addBalance возвращает false для неизвестного пользователя")
    void addBalanceReturnsFalseForUnknownUser() {
        Assertions.assertFalse(repository.addBalance(connection, USER_ID, 1));
    }

    @Test
    @DisplayName("Отрицательные суммы отклоняются")
    void negativeAmountsAreRejected() throws Exception {
        saveUser(10, 10);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.spendBalance(connection, USER_ID, -1));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.spendShards(connection, USER_ID, -1));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.addBalance(connection, USER_ID, -1));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.addShards(connection, USER_ID, -1));
    }
}
