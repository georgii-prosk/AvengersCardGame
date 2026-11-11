package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;
import org.jetbrains.annotations.NotNull;

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
    public void addCard(@NotNull Connection connection, long userId, long heroId) {
        cards.computeIfAbsent(userId, id -> new HashMap<>()).merge(heroId, 1, Integer::sum);
    }

    @Override
    public boolean removeCard(@NotNull Connection connection, long userId, long heroId) {
        Map<Long, Integer> userCards = cards.get(userId);
        if (userCards == null) return false;
        Integer count = userCards.get(heroId);
        if (count == null || count <= 0) return false;
        if (count == 1) {
            userCards.remove(heroId);
            if (userCards.isEmpty()) cards.remove(userId);
        } else {
            userCards.put(heroId, count - 1);
        }
        return true;
    }

    @Override
    public @NotNull List<UserCard> findByUserId(long userId) {
        List<UserCard> result = new ArrayList<>();
        cards.getOrDefault(userId, Map.of())
                .forEach((heroId, count) -> result.add(new UserCard(userId, heroId, count)));
        return result;
    }

    @Override
    public int countByUserId(long userId) {
        return cards.getOrDefault(userId, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
    }

    /**
     * Возвращает количество копий конкретной карточки.
     *
     * @param userId идентификатор пользователя
     * @param heroId идентификатор героя
     * @return количество копий
     */
    public int countOf(long userId, long heroId) {
        return cards.getOrDefault(userId, Map.of()).getOrDefault(heroId, 0);
    }
}
