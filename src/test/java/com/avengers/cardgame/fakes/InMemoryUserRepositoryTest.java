package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.OptionalInt;

/**
 * Тесты для {@link InMemoryUserRepository}.
 */
class InMemoryUserRepositoryTest {

    /** Идентификатор пользователя для тестов. */
    private static final long USER_ID = 1L;

    /** Время создания профиля для тестов. */
    private final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Репозиторий пользователей. */
    private InMemoryUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    /**
     * Создаёт и сохраняет пользователя с указанными балансами.
     *
     * @param balance баланс валюты
     * @param shards  баланс кусочков
     * @return сохранённый пользователь
     * @throws Exception если сохранение невозможно
     */
    private User saveUser(int balance, int shards) throws Exception {
        User user = User.of(USER_ID, PlayerName.of("Tony"), null,
                balance, shards, 0, CREATED_AT, CREATED_AT);
        repository.save(user);
        return user;
    }

    /**
     * Проверяет сохранение и поиск пользователя.
     */
    @Test
    @DisplayName("Сохраняет и находит пользователя")
    void savesAndFindsUser() throws Exception {
        saveUser(10, 5);

        Assertions.assertTrue(repository.findByUserId(USER_ID).isPresent());
        Assertions.assertTrue(repository.findByUserId(2L).isEmpty());
        Assertions.assertEquals(1, repository.size());
    }

    /**
     * Проверяет, что повторное сохранение отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при повторном сохранении")
    void throwsOnDuplicateSave() throws Exception {
        User user = saveUser(10, 5);

        Assertions.assertThrows(UserAlreadyExistsException.class, () -> repository.save(user));
    }

    /**
     * Проверяет списание валюты.
     */
    @Test
    @DisplayName("Списывает валюту при достаточном балансе")
    void spendsBalance() throws Exception {
        saveUser(100, 0);

        OptionalInt balance = repository.spendBalance(USER_ID, 40);

        Assertions.assertEquals(60, balance.orElseThrow());
        Assertions.assertEquals(60, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет, что списание больше баланса не меняет состояние.
     */
    @Test
    @DisplayName("Не списывает валюту при недостатке")
    void doesNotSpendBalanceWhenInsufficient() throws Exception {
        saveUser(30, 0);

        Assertions.assertTrue(repository.spendBalance(USER_ID, 40).isEmpty());
        Assertions.assertEquals(30, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет, что списание у отсутствующего пользователя возвращает пустой результат.
     */
    @Test
    @DisplayName("Не списывает валюту у несуществующего пользователя")
    void doesNotSpendBalanceForMissingUser() {
        Assertions.assertTrue(repository.spendBalance(USER_ID, 10).isEmpty());
    }

    /**
     * Проверяет списание кусочков.
     */
    @Test
    @DisplayName("Списывает кусочки при достаточном балансе")
    void spendsShards() throws Exception {
        saveUser(0, 20);

        OptionalInt balance = repository.spendShards(USER_ID, 15);

        Assertions.assertEquals(5, balance.orElseThrow());
        Assertions.assertEquals(5, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что списание кусочков больше баланса не меняет состояние.
     */
    @Test
    @DisplayName("Не списывает кусочки при недостатке")
    void doesNotSpendShardsWhenInsufficient() throws Exception {
        saveUser(0, 5);

        Assertions.assertTrue(repository.spendShards(USER_ID, 15).isEmpty());
        Assertions.assertEquals(5, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что неположительное количество для списания отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при неположительном списании")
    void throwsOnNonPositiveSpend() throws Exception {
        saveUser(100, 100);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.spendBalance(USER_ID, 0));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.spendShards(USER_ID, -1));
    }

    /**
     * Проверяет начисление валюты.
     */
    @Test
    @DisplayName("Начисляет валюту")
    void addsBalance() throws Exception {
        saveUser(10, 0);

        Assertions.assertTrue(repository.addBalance(USER_ID, 90));
        Assertions.assertEquals(100, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет начисление кусочков.
     */
    @Test
    @DisplayName("Начисляет кусочки")
    void addsShards() throws Exception {
        saveUser(0, 10);

        OptionalInt balance = repository.addShards(USER_ID, 5);

        Assertions.assertEquals(15, balance.orElseThrow());
        Assertions.assertEquals(15, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что отрицательное начисление отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательном начислении")
    void throwsOnNegativeAddition() throws Exception {
        saveUser(10, 10);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.addBalance(USER_ID, -1));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.addShards(USER_ID, -1));
    }

    /**
     * Проверяет, что начисление отсутствующему пользователю не выполняется.
     */
    @Test
    @DisplayName("Не начисляет ресурсы несуществующему пользователю")
    void doesNotAddToMissingUser() {
        Assertions.assertFalse(repository.addBalance(USER_ID, 10));
        Assertions.assertTrue(repository.addShards(USER_ID, 10).isEmpty());
    }
}
