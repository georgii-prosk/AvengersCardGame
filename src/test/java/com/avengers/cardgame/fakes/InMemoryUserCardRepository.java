package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.exception.InsufficientShardsException;
import com.avengers.cardgame.exception.NotEnoughCardsException;
import com.avengers.cardgame.exception.UserNotFoundException;
import com.avengers.cardgame.model.User;
import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * Репозиторий карточек пользователей в памяти.
 * <p>
 * Составные операции экономики требуют ссылку на репозиторий пользователей,
 * чтобы атомарно изменять карточки и баланс кусочков.
 */
public class InMemoryUserCardRepository implements UserCardRepository {

    /** Количество копий: пользователь → (герой → количество). */
    private final Map<Long, Map<Long, Integer>> cards = new HashMap<>();

    /** Репозиторий пользователей для составных операций, может быть {@code null}. */
    private final InMemoryUserRepository userRepository;

    /**
     * Создаёт репозиторий без поддержки составных операций экономики.
     */
    public InMemoryUserCardRepository() {
        this(null);
    }

    /**
     * @param userRepository репозиторий пользователей для составных операций, может быть {@code null}
     */
    public InMemoryUserCardRepository(@Nullable InMemoryUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void addCard(long userId, long heroId) {
        cards.computeIfAbsent(userId, _ -> new HashMap<>()).merge(heroId, 1, Integer::sum);
    }

    @Override
    public int disassemble(long userId, long heroId, int count, int shardsGain) throws AppException {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to remove must be positive");
        }
        if (userRepository == null) {
            throw new IllegalStateException("User repository is required for economy operations");
        }
        Map<Long, User> userSnapshot = userRepository.snapshot();
        Map<Long, Map<Long, Integer>> cardSnapshot = snapshot();
        try {
            if (!removeCard(userId, heroId, count)) {
                throw new NotEnoughCardsException(heroId, count, countByUserIdAndHeroId(userId, heroId));
            }
            OptionalInt balance = userRepository.addShards(userId, shardsGain);
            if (balance.isEmpty()) {
                throw new UserNotFoundException(userId);
            }
            return balance.getAsInt();
        } catch (AppException | RuntimeException exception) {
            userRepository.restore(userSnapshot);
            restore(cardSnapshot);
            throw exception;
        }
    }

    @Override
    public int craft(long userId, long heroId, int shardsCost) throws AppException {
        if (shardsCost <= 0) {
            throw new IllegalArgumentException("Craft cost must be positive");
        }
        if (userRepository == null) {
            throw new IllegalStateException("User repository is required for economy operations");
        }
        Map<Long, User> userSnapshot = userRepository.snapshot();
        Map<Long, Map<Long, Integer>> cardSnapshot = snapshot();
        try {
            OptionalInt balance = userRepository.spendShards(userId, shardsCost);
            if (balance.isEmpty()) {
                OptionalInt available = userRepository.findShards(userId);
                if (available.isEmpty()) {
                    throw new UserNotFoundException(userId);
                }
                throw new InsufficientShardsException(shardsCost, available.getAsInt());
            }
            addCard(userId, heroId);
            return balance.getAsInt();
        } catch (AppException | RuntimeException exception) {
            userRepository.restore(userSnapshot);
            restore(cardSnapshot);
            throw exception;
        }
    }

    /**
     * Удаляет указанное количество копий карточки.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @param count  количество копий для удаления
     * @return {@code true}, если удаление выполнено; {@code false}, если копий недостаточно
     */
    public boolean removeCard(long userId, long heroId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count to remove must be positive");
        }
        Map<Long, Integer> userCards = cards.get(userId);
        if (userCards == null) {
            return false;
        }
        Integer current = userCards.get(heroId);
        if (current == null || current < count) {
            return false;
        }
        int remaining = current - count;
        if (remaining == 0) {
            userCards.remove(heroId);
        } else {
            userCards.put(heroId, remaining);
        }
        return true;
    }

    /**
     * Возвращает все карточки пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список карточек
     */
    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        List<UserCard> result = new ArrayList<>();
        cards.getOrDefault(userId, Map.of())
                .forEach((heroId, count) -> result.add(new UserCard(userId, heroId, count)));
        return result;
    }

    /**
     * Считает общее количество карточек у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return количество карточек
     */
    public int countByUserId(long userId) {
        return cards.getOrDefault(userId, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int countByUserIdAndHeroId(long userId, long heroId) {
        return cards.getOrDefault(userId, Map.of()).getOrDefault(heroId, 0);
    }

    /**
     * Возвращает снимок хранилища для отката составных операций.
     *
     * @return копия хранилища
     */
    Map<Long, Map<Long, Integer>> snapshot() {
        Map<Long, Map<Long, Integer>> copy = new HashMap<>();
        cards.forEach((userId, heroMap) -> copy.put(userId, new HashMap<>(heroMap)));
        return copy;
    }

    /**
     * Восстанавливает хранилище из снимка.
     *
     * @param snapshot снимок хранилища
     */
    void restore(Map<Long, Map<Long, Integer>> snapshot) {
        cards.clear();
        snapshot.forEach((userId, heroMap) -> cards.put(userId, new HashMap<>(heroMap)));
    }
}
