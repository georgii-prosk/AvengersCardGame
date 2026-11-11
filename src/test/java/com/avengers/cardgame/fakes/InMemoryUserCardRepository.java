package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Репозиторий карточек пользователей в памяти.
 */
public class InMemoryUserCardRepository implements UserCardRepository {

    /** Количество копий: пользователь → (герой → количество). */
    private final Map<Long, Map<Long, Integer>> cards = new HashMap<>();

    @Override
    public void addCard(long userId, long heroId) {
        addCard(null, userId, heroId);
    }

    @Override
    public void addCard(@Nullable Connection connection, long userId, long heroId) {
        cards.computeIfAbsent(userId, _ -> new HashMap<>()).merge(heroId, 1, Integer::sum);
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
        return removeCard(null, userId, heroId, count);
    }

    @Override
    public boolean removeCard(@Nullable Connection connection, long userId, long heroId, int count) {
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
}
