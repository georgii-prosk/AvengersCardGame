package com.avengers.cardgame.fakes;

import com.avengers.cardgame.model.Hero;
import com.avengers.cardgame.service.DailyRewardResult;
import com.avengers.cardgame.service.DailyRewardService;

import java.time.Duration;
import java.util.List;

/**
 * Фейковый сервис ежедневной награды: выдаёт заданных героев или кулдаун.
 */
public class FakeDailyRewardService implements DailyRewardService {

    /** Герои, которые выдаёт фейк. */
    private final List<Hero> heroes;

    /** Кулдаун, если награда недоступна. */
    private final Duration cooldown;

    /** Идентификатор последнего обратившегося пользователя. */
    private long lastUserId;

    /**
     * @param heroes герои, которые будет выдавать фейк
     */
    public FakeDailyRewardService(List<Hero> heroes) {
        this.heroes = List.copyOf(heroes);
        this.cooldown = null;
    }

    /**
     * @param cooldown оставшееся время до следующей награды
     */
    public FakeDailyRewardService(Duration cooldown) {
        this.heroes = List.of();
        this.cooldown = cooldown;
    }

    @Override
    public DailyRewardResult grantDaily(long userId) {
        lastUserId = userId;
        if (cooldown != null) {
            return new DailyRewardResult.Cooldown(cooldown);
        }
        return new DailyRewardResult.Success(heroes);
    }

    /**
     * @return идентификатор последнего обратившегося пользователя
     */
    public long lastUserId() {
        return lastUserId;
    }
}
