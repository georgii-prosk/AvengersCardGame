package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Фейковый репозиторий пользователей в памяти.
 */
public class InMemoryUserRepository implements UserRepository {

    /** Хранилище пользователей по идентификатору. */
    private final Map<Long, User> storage = new HashMap<>();

    @Override
    public @NotNull Optional<User> findByUserId(long userId) {
        return Optional.ofNullable(storage.get(userId));
    }

    @Override
    public void save(@NotNull User user) throws UserAlreadyExistsException {
        if (storage.containsKey(user.userId())) {
            throw new UserAlreadyExistsException(user.userId());
        }
        storage.put(user.userId(), user);
    }

    @Override
    public void update(@NotNull User user) throws UserNotFoundException {
        if (!storage.containsKey(user.userId())) {
            throw new UserNotFoundException(user.userId());
        }
        storage.put(user.userId(), user);
    }

    @Override
    public void updateLastDailyAt(long userId, @NotNull LocalDateTime lastDailyAt) throws UserNotFoundException {
        User user = storage.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        storage.put(userId, user.withLastDailyAt(lastDailyAt));
    }

    @Override
    public @NotNull List<User> findUsersToNotify(@NotNull LocalDateTime thresholdTime) {
        return storage.values().stream()
                .filter(user -> user.lastDailyAt() != null)
                .filter(user -> !user.lastDailyAt().isAfter(thresholdTime))
                .filter(user -> user.dailyNotifiedAt() == null
                        || user.dailyNotifiedAt().isBefore(user.lastDailyAt()))
                .toList();
    }

    @Override
    public void updateDailyNotifiedAt(long userId, @NotNull LocalDateTime notifiedAt) throws UserNotFoundException {
        User user = storage.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        storage.put(userId, user.withDailyNotifiedAt(notifiedAt));
    }

    @Override
    public boolean spendBalance(@NotNull Connection connection, long userId, int amount) {
        return spend(userId, amount, true);
    }

    @Override
    public boolean spendShards(@NotNull Connection connection, long userId, int amount) {
        return spend(userId, amount, false);
    }

    @Override
    public boolean addBalance(@NotNull Connection connection, long userId, int amount) {
        requireNonNegative(amount);
        User user = storage.get(userId);
        if (user == null) return false;
        storage.put(userId, user.withBalance(user.balance() + amount));
        return true;
    }

    @Override
    public boolean addShards(@NotNull Connection connection, long userId, int amount) {
        requireNonNegative(amount);
        User user = storage.get(userId);
        if (user == null) return false;
        storage.put(userId, user.withShards(user.shards() + amount));
        return true;
    }

    /**
     * Возвращает количество сохранённых пользователей.
     *
     * @return количество пользователей
     */
    public int size() {
        return storage.size();
    }

    /**
     * Списывает валюту или кусочки, если средств достаточно.
     *
     * @param userId   идентификатор пользователя
     * @param amount   сумма списания
     * @param currency {@code true} для валюты, {@code false} для кусочков
     * @return {@code true}, если списание выполнено
     */
    private boolean spend(long userId, int amount, boolean currency) {
        requireNonNegative(amount);
        User user = storage.get(userId);
        if (user == null) return false;
        int available = currency ? user.balance() : user.shards();
        if (available < amount) return false;
        storage.put(userId, currency
                ? user.withBalance(available - amount)
                : user.withShards(available - amount));
        return true;
    }

    /**
     * Проверяет, что сумма операции неотрицательная.
     *
     * @param amount сумма операции
     * @throws IllegalArgumentException если сумма отрицательная
     */
    private void requireNonNegative(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Amount must be non-negative");
    }
}