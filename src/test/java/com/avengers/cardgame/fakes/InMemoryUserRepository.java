package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return findByUserId(null, userId);
    }

    @Override
    public @NotNull Optional<User> findByUserId(@Nullable Connection connection, long userId) {
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
    public boolean spendBalance(@Nullable Connection connection, long userId, int amount) {
        return spend(userId, amount, User::balance, User::withBalance);
    }

    @Override
    public boolean spendShards(@Nullable Connection connection, long userId, int amount) {
        return spend(userId, amount, User::shards, User::withShards);
    }

    @Override
    public void addBalance(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException {
        User user = storage.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add must be non-negative");
        }
        storage.put(userId, user.withBalance(user.balance() + amount));
    }

    @Override
    public void addShards(@Nullable Connection connection, long userId, int amount) throws UserNotFoundException {
        User user = storage.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add must be non-negative");
        }
        storage.put(userId, user.withShards(user.shards() + amount));
    }

    /**
     * Списывает ресурс с баланса пользователя.
     *
     * @param userId     идентификатор пользователя
     * @param amount     количество для списания
     * @param balance    функция чтения баланса
     * @param withAmount функция создания копии пользователя с новым балансом
     * @return {@code true}, если списание выполнено
     */
    private boolean spend(long userId, int amount,
                          java.util.function.ToIntFunction<User> balance,
                          java.util.function.BiFunction<User, Integer, User> withAmount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive");
        }
        User user = storage.get(userId);
        if (user == null || balance.applyAsInt(user) < amount) {
            return false;
        }
        storage.put(userId, withAmount.apply(user, balance.applyAsInt(user) - amount));
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
}
