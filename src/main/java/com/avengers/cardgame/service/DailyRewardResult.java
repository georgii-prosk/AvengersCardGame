package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import java.time.Duration;
import java.util.List;

/**
 * Результат выполнения команды /daily.
 *
 * @param heroes            список выпавших героев (пустой, если сработал кулдаун)
 * @param timeUntilNextClaim оставшееся время до следующей выдачи (null, если награда получена)
 */
public record DailyRewardResult(List<Hero> heroes, Duration timeUntilNextClaim) {

    /**
     * Создаёт результат с защитной копией списка героев.
     */
    public DailyRewardResult {
        heroes = List.copyOf(heroes);
    }

    /**
     * @return {@code true}, если награда была успешно получена; {@code false}, если активен кулдаун
     */
    public boolean claimed() {
        return timeUntilNextClaim == null;
    }

    /**
     * Создаёт результат успешной выдачи награды.
     *
     * @param heroes выпавшие герои
     * @return результат
     */
    public static DailyRewardResult success(List<Hero> heroes) {
        return new DailyRewardResult(heroes, null);
    }

    /**
     * Создаёт результат с информацией о кулдауне.
     *
     * @param timeLeft оставшееся время
     * @return результат
     */
    public static DailyRewardResult cooldown(Duration timeLeft) {
        return new DailyRewardResult(List.of(), timeLeft);
    }
}