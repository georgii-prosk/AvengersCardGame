package com.avengers.cardgame.model;

import com.avengers.cardgame.exception.InvalidUserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Тесты для {@link User}.
 */
class UserTest {

    /** Время создания профиля для тестов. */
    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2026, 1, 1, 12, 0);

    /** Время обновления профиля для тестов. */
    private static final LocalDateTime UPDATED_AT = CREATED_AT.plusHours(1);

    /**
     * Создаёт тестового пользователя.
     *
     * @return профиль пользователя
     * @throws Exception если данные некорректны
     */
    private User user() throws Exception {
        return User.of(1L, PlayerName.of("Tony"), new Bio("Genius"),
                100, 10, 3, CREATED_AT, UPDATED_AT);
    }

    /**
     * Проверяет создание профиля с кусочками.
     */
    @Test
    @DisplayName("Создаёт профиль с кусочками")
    void createsUserWithShards() throws Exception {
        User user = user();

        Assertions.assertEquals(1L, user.userId());
        Assertions.assertEquals(100, user.balance());
        Assertions.assertEquals(10, user.shards());
        Assertions.assertEquals(3, user.cardCount());
    }

    /**
     * Проверяет, что старый фабричный метод задаёт нулевые кусочки.
     */
    @Test
    @DisplayName("Старый фабричный метод задаёт ноль кусочков")
    void legacyFactorySetsZeroShards() throws Exception {
        User user = User.of(1L, PlayerName.of("Tony"), null,
                100, 3, CREATED_AT, UPDATED_AT);

        Assertions.assertEquals(0, user.shards());
    }

    /**
     * Проверяет, что отрицательное количество кусочков отклоняется.
     */
    @Test
    @DisplayName("Бросает исключение при отрицательных кусочках")
    void throwsOnNegativeShards() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User(1L, PlayerName.of("Tony"), null,
                        0, -1, 0, CREATED_AT, UPDATED_AT, null, null));
    }

    /**
     * Проверяет, что {@link User#of} оборачивает ошибку в {@link InvalidUserException}.
     */
    @Test
    @DisplayName("of() оборачивает ошибку в InvalidUserException")
    void ofWrapsValidationError() {
        Assertions.assertThrows(InvalidUserException.class,
                () -> User.of(1L, PlayerName.of("Tony"), null,
                        0, -5, 0, CREATED_AT, UPDATED_AT));
    }

    /**
     * Проверяет, что {@link User#withShards(int)} меняет только кусочки.
     */
    @Test
    @DisplayName("withShards меняет только баланс кусочков")
    void withShardsChangesOnlyShards() throws Exception {
        User original = user();

        User updated = original.withShards(42);

        Assertions.assertEquals(42, updated.shards());
        Assertions.assertEquals(original.balance(), updated.balance());
        Assertions.assertEquals(original.cardCount(), updated.cardCount());
        Assertions.assertEquals(original.playerName(), updated.playerName());
        Assertions.assertEquals(original.bio(), updated.bio());
    }

    /**
     * Проверяет, что {@link User#withBalance(int)} сохраняет кусочки.
     */
    @Test
    @DisplayName("withBalance сохраняет кусочки")
    void withBalanceKeepsShards() throws Exception {
        User updated = user().withBalance(250);

        Assertions.assertEquals(250, updated.balance());
        Assertions.assertEquals(10, updated.shards());
    }

    /**
     * Проверяет, что {@link User#withPlayerName(PlayerName)} сохраняет кусочки.
     */
    @Test
    @DisplayName("withPlayerName сохраняет кусочки")
    void withPlayerNameKeepsShards() throws Exception {
        User updated = user().withPlayerName(PlayerName.of("Pepper"));

        Assertions.assertEquals("Pepper", updated.playerName().value());
        Assertions.assertEquals(10, updated.shards());
    }

    /**
     * Проверяет, что {@link User#withBio(Bio)} сохраняет кусочки.
     */
    @Test
    @DisplayName("withBio сохраняет кусочки")
    void withBioKeepsShards() throws Exception {
        User updated = user().withBio(new Bio("CEO"));

        Assertions.assertEquals("CEO", Objects.requireNonNull(updated.bio()).value());
        Assertions.assertEquals(10, updated.shards());
    }

    /**
     * Проверяет, что {@link User#withLastDailyAt(LocalDateTime)} сохраняет кусочки.
     */
    @Test
    @DisplayName("withLastDailyAt сохраняет кусочки")
    void withLastDailyAtKeepsShards() throws Exception {
        LocalDateTime lastDailyAt = CREATED_AT.plusDays(1);

        User updated = user().withLastDailyAt(lastDailyAt);

        Assertions.assertEquals(lastDailyAt, updated.lastDailyAt());
        Assertions.assertEquals(10, updated.shards());
    }

    /**
     * Проверяет, что {@link User#withDailyNotifiedAt(LocalDateTime)} сохраняет кусочки.
     */
    @Test
    @DisplayName("withDailyNotifiedAt сохраняет кусочки")
    void withDailyNotifiedAtKeepsShards() throws Exception {
        LocalDateTime notifiedAt = CREATED_AT.plusDays(2);

        User updated = user().withDailyNotifiedAt(notifiedAt);

        Assertions.assertEquals(notifiedAt, updated.dailyNotifiedAt());
        Assertions.assertEquals(10, updated.shards());
    }

    /**
     * Проверяет, что время обновления не может быть раньше времени создания.
     */
    @Test
    @DisplayName("Бросает исключение, если updatedAt раньше createdAt")
    void throwsWhenUpdatedBeforeCreated() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new User(1L, PlayerName.of("Tony"), null,
                        0, 0, 0, UPDATED_AT, CREATED_AT, null, null));
    }
}
