package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.CardService;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.Cooldown;
import com.avengers.cardgame.service.Success;

import java.time.Duration;
import java.util.List;

/**
 * Тестовый сервис карточек с заранее заданным результатом.
 */
public class FakeCardService implements CardService {

    private final List<Hero> heroes;
    private final Duration cooldown;
    private long lastUserId;

    public FakeCardService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
        this.cooldown = null;
    }

    public FakeCardService(Duration cooldown) {
        this.heroes = List.of();
        this.cooldown = cooldown;
    }

    @Override
    public DailyRewardResult grantDaily(long userId) {
        lastUserId = userId;
        if (cooldown != null) {
            return new Cooldown(cooldown);
        }
        return new Success(heroes);
    }

    public long lastUserId() {
        return lastUserId;
    }
}
