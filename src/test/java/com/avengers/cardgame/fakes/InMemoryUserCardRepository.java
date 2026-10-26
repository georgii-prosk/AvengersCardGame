package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.UserCard;
import com.avengers.cardgame.repository.UserCardRepository;

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
        cards.computeIfAbsent(userId, id -> new HashMap<>()).merge(heroId, 1, Integer::sum);
    }


    @Override
    public void addCard(Connection conn, long userId, long heroId) {
        addCard(userId, heroId);
    }

    @Override
    public List<UserCard> findByUserId(long userId) {
        List<UserCard> result = new ArrayList<>();
        cards.getOrDefault(userId, Map.of())
                .forEach((heroId, count) -> result.add(new UserCard(userId, heroId, count)));
        return result;
    }

    @Override
    public int countByUserId(long userId) {
        return cards.getOrDefault(userId, Map.of()).values().stream().mapToInt(Integer::intValue).sum();
    }
}
