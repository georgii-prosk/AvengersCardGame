package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.PlayerName;
import com.avengers.cardgame.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link InMemoryUserRepository}.
 */
class InMemoryUserRepositoryTest {

    /** Идентификатор пользователя для тестов. */
    private static final long USER_ID = 1L;

    /** Время создания профиля для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

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

        boolean spent = repository.spendBalance(null, USER_ID, 40);

        Assertions.assertTrue(spent);
        Assertions.assertEquals(60, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет, что списание больше баланса не меняет состояние.
     */
    @Test
    @DisplayName("Не списывает валюту при недостатке")
    void doesNotSpendBalanceWhenInsufficient() throws Exception {
        saveUser(30, 0);

        boolean spent = repository.spendBalance(null, USER_ID, 40);

        Assertions.assertFalse(spent);
        Assertions.assertEquals(30, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет, что списание у отсутствующего пользователя возвращает {@code false}.
     */
    @Test
    @DisplayName("Не списывает валюту у несуществующего пользователя")
    void doesNotSpendBalanceForMissingUser() {
        Assertions.assertFalse(repository.spendBalance(null, USER_ID, 10));
    }

    /**
     * Проверяет списание кусочков.
     */
    @Test
    @DisplayName("Списывает кусочки при достаточном балансе")
    void spendsShards() throws Exception {
        saveUser(0, 20);

        boolean spent = repository.spendShards(null, USER_ID, 15);

        Assertions.assertTrue(spent);
        Assertions.assertEquals(5, repository.findByUserId(USER_ID).orElseThrow().shards());
    }

    /**
     * Проверяет, что списание кусочков больше баланса не меняет состояние.
     */
    @Test
    @DisplayName("Не списывает кусочки при недостатке")
    void doesNotSpendShardsWhenInsufficient() throws Exception {
        saveUser(0, 5);

        boolean spent = repository.spendShards(null, USER_ID, 15);

        Assertions.assertFalse(spent);
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
                () -> repository.spendBalance(null, USER_ID, 0));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.spendShards(null, USER_ID, -1));
    }

    /**
     * Проверяет начисление валюты.
     */
    @Test
    @DisplayName("Начисляет валюту")
    void addsBalance() throws Exception {
        saveUser(10, 0);

        repository.addBalance(null, USER_ID, 90);

        Assertions.assertEquals(100, repository.findByUserId(USER_ID).orElseThrow().balance());
    }

    /**
     * Проверяет начисление кусочков.
     */
    @Test
    @DisplayName("Начисляет кусочки")
    void addsShards() throws Exception {
        saveUser(0, 10);

        repository.addShards(null, USER_ID, 5);

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
                () -> repository.addBalance(null, USER_ID, -1));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.addShards(null, USER_ID, -1));
    }

    /**
     * Проверяет, что начисление отсутствующему пользователю отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при начислении несуществующему пользователю")
    void throwsWhenAddingToMissingUser() {
        Assertions.assertThrows(UserNotFoundException.class,
                () -> repository.addBalance(null, USER_ID, 10));
        Assertions.assertThrows(UserNotFoundException.class,
                () -> repository.addShards(null, USER_ID, 10));
    }
}
