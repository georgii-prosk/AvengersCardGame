package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

/**
 * Результат выдачи ежедневной награды.
 */
public sealed interface DailyRewardResult permits DailyRewardResult.Cooldown, DailyRewardResult.Success {

    /**
     * Кулдаун ежедневной награды ещё не кончился.
     *
     * @param timeUntilNextClaim оставшееся время
     */
    record Cooldown(@NotNull Duration timeUntilNextClaim) implements DailyRewardResult {
    }

    /**
     * Ежедневная награда выдана.
     *
     * @param heroes список выпавших героев
     */
    record Success(@NotNull List<Hero> heroes) implements DailyRewardResult {

        /**
         * Создаёт результат с защитной копией списка героев.
         */
        public Success {
            heroes = List.copyOf(heroes);
        }
    }
}
