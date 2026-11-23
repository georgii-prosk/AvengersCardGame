package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import org.jetbrains.annotations.NotNull;
import java.util.List;

/**
 * Ежедневная награда выдана.
 *
 * @param heroes список выпавших героев
 */
public record Success(@NotNull List<Hero> heroes) implements DailyRewardResult {

    /**
     * Создаёт результат с защитной копией списка героев.
     */
    public Success {
        heroes = List.copyOf(heroes);
    }
}