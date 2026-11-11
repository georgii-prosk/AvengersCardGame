package com.avengers.cardgame.model;

import com.avengers.cardgame.support.NullParameterAssertion;

import com.avengers.cardgame.exception.InvalidUserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

/**
 * Тесты для {@link User}.
 */
class UserTest {

    /** Время создания профиля, используемое в тестах. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Время обновления профиля, используемое в тестах. */
    private static final LocalDateTime UPDATED_AT = CREATED_AT.plusDays(1);

    /**
     * Создаёт пользователя с заданными балансом и кусочками.
     *
     * @param balance баланс валюты
     * @param shards  количество кусочков
     * @return пользователь
     */
    private User user(int balance, int shards) {
        return new User(1L, new PlayerName("Tony"), new Bio("Genius"), balance, shards, 3,
                CREATED_AT, UPDATED_AT, null, null);
    }

    @Test
    @DisplayName("Создаёт профиль с валидными данными")
    void createsValidUser() {
        User user = user(100, 7);

        Bio bio = user.bio();
        Assertions.assertNotNull(bio);
        Assertions.assertEquals(1L, user.userId());
        Assertions.assertEquals("Tony", user.playerName().value());
        Assertions.assertEquals("Genius", bio.value());
        Assertions.assertEquals(100, user.balance());
        Assertions.assertEquals(7, user.shards());
        Assertions.assertEquals(3, user.cardCount());
    }

    @Test
    @DisplayName("withBalance меняет только баланс")
    void withBalanceKeepsOtherFields() {
        User updated = user(100, 7).withBalance(50);

        Assertions.assertEquals(50, updated.balance());
        Assertions.assertEquals(7, updated.shards());
        Assertions.assertEquals(3, updated.cardCount());
        Assertions.assertEquals("Tony", updated.playerName().value());
    }

    @Test
    @DisplayName("withShards меняет только кусочки")
    void withShardsKeepsOtherFields() {
        User updated = user(100, 7).withShards(21);

        Assertions.assertEquals(21, updated.shards());
        Assertions.assertEquals(100, updated.balance());
        Assertions.assertEquals(3, updated.cardCount());
    }

    @Test
    @DisplayName("withPlayerName меняет только имя")
    void withPlayerNameKeepsOtherFields() {
        User updated = user(100, 7).withPlayerName(new PlayerName("Steve"));

        Assertions.assertEquals("Steve", updated.playerName().value());
        Assertions.assertEquals(100, updated.balance());
        Assertions.assertEquals(7, updated.shards());
    }

    @Test
    @DisplayName("withBio меняет только описание")
    void withBioKeepsOtherFields() {
        User updated = user(100, 7).withBio(new Bio("New bio"));

        Bio bio = updated.bio();
        Assertions.assertNotNull(bio);
        Assertions.assertEquals("New bio", bio.value());
        Assertions.assertEquals(100, updated.balance());
        Assertions.assertEquals(7, updated.shards());
    }

    @Test
    @DisplayName("withLastDailyAt меняет только время награды")
    void withLastDailyAtKeepsOtherFields() {
        LocalDateTime daily = UPDATED_AT.plusHours(1);

        User updated = user(100, 7).withLastDailyAt(daily);

        Assertions.assertEquals(daily, updated.lastDailyAt());
        Assertions.assertEquals(7, updated.shards());
    }

    @Test
    @DisplayName("withDailyNotifiedAt меняет только время уведомления")
    void withDailyNotifiedAtKeepsOtherFields() {
        LocalDateTime notified = UPDATED_AT.plusHours(2);

        User updated = user(100, 7).withDailyNotifiedAt(notified);

        Assertions.assertEquals(notified, updated.dailyNotifiedAt());
        Assertions.assertEquals(7, updated.shards());
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном балансе")
    void throwsOnNegativeBalance() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> user(-1, 0));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном количестве кусочков")
    void throwsOnNegativeShards() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> user(0, -1));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном количестве карточек")
    void throwsOnNegativeCardCount() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User(
                1L, new PlayerName("Tony"), null, 0, 0, -1, CREATED_AT, UPDATED_AT, null, null));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо имени")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullPlayerName() {
        NullParameterAssertion.assertRejected(() -> new User(
                1L, null, null, 0, 0, 0, CREATED_AT, UPDATED_AT, null, null));
    }

    @Test
    @DisplayName("Бросает исключение, если updatedAt раньше createdAt")
    void throwsWhenUpdatedBeforeCreated() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> new User(
                1L, new PlayerName("Tony"), null, 0, 0, 0,
                CREATED_AT, CREATED_AT.minusDays(1), null, null));
    }

    @Test
    @DisplayName("User.of пробрасывает ошибку в виде InvalidUserException")
    void ofWrapsValidationError() {
        Assertions.assertThrows(InvalidUserException.class, () -> User.of(
                1L, new PlayerName("Tony"), null, 0, -1, 0, CREATED_AT, UPDATED_AT));
    }

    @Test
    @DisplayName("User.of с кусочками сохраняет их")
    void ofKeepsShards() throws Exception {
        User created = User.of(1L, new PlayerName("Tony"), null, 100, 42, 0,
                CREATED_AT, UPDATED_AT, null, null);

        Assertions.assertEquals(42, created.shards());
        Assertions.assertEquals(100, created.balance());
    }
}
