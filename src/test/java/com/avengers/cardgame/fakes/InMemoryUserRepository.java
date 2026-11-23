package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.UserAlreadyExistsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.repository.UserRepository;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;

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

    /**
     * Списывает валюту с баланса пользователя.
     *
     * @param userId идентификатор пользователя
     * @param amount сумма списания
     * @return новый баланс, если списание выполнено
     */
    OptionalInt spendBalance(long userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive");
        }
        User user = storage.get(userId);
        if (user == null || user.balance() < amount) {
            return OptionalInt.empty();
        }
        int newBalance = user.balance() - amount;
        storage.put(userId, user.withBalance(newBalance));
        return OptionalInt.of(newBalance);
    }

    /**
     * Списывает кусочки с баланса пользователя.
     *
     * @param userId идентификатор пользователя
     * @param amount количество кусочков
     * @return новый баланс, если списание выполнено
     */
    OptionalInt spendShards(long userId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to spend must be positive");
        }
        User user = storage.get(userId);
        if (user == null || user.shards() < amount) {
            return OptionalInt.empty();
        }
        int newBalance = user.shards() - amount;
        storage.put(userId, user.withShards(newBalance));
        return OptionalInt.of(newBalance);
    }

    /**
     * Начисляет кусочки на баланс пользователя.
     *
     * @param userId идентификатор пользователя
     * @param amount количество кусочков
     * @return новый баланс, если пользователь найден
     */
    OptionalInt addShards(long userId, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add must be non-negative");
        }
        User user = storage.get(userId);
        if (user == null) {
            return OptionalInt.empty();
        }
        int newBalance = user.shards() + amount;
        storage.put(userId, user.withShards(newBalance));
        return OptionalInt.of(newBalance);
    }

    /**
     * Начисляет валюту на баланс пользователя.
     *
     * @param userId идентификатор пользователя
     * @param amount сумма начисления
     * @return {@code true}, если пользователь найден; иначе {@code false}
     */
    boolean addBalance(long userId, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add must be non-negative");
        }
        User user = storage.get(userId);
        if (user == null) {
            return false;
        }
        storage.put(userId, user.withBalance(user.balance() + amount));
        return true;
    }

    /**
     * Читает баланс валюты пользователя.
     *
     * @param userId идентификатор пользователя
     * @return баланс, если пользователь найден
     */
    OptionalInt findBalance(long userId) {
        User user = storage.get(userId);
        return user == null ? OptionalInt.empty() : OptionalInt.of(user.balance());
    }

    /**
     * Читает баланс кусочков пользователя.
     *
     * @param userId идентификатор пользователя
     * @return баланс, если пользователь найден
     */
    OptionalInt findShards(long userId) {
        User user = storage.get(userId);
        return user == null ? OptionalInt.empty() : OptionalInt.of(user.shards());
    }

    /**
     * Возвращает снимок хранилища для отката составных операций.
     *
     * @return копия хранилища
     */
    Map<Long, User> snapshot() {
        return new HashMap<>(storage);
    }

    /**
     * Восстанавливает хранилище из снимка.
     *
     * @param snapshot снимок хранилища
     */
    void restore(Map<Long, User> snapshot) {
        storage.clear();
        storage.putAll(snapshot);
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
