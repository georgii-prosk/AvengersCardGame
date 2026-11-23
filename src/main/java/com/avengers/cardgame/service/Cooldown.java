package com.avengers.cardgame.service;

import org.jetbrains.annotations.NotNull;
import java.time.Duration;

/**
 * Кулдаун у ежедневной награды ещё не кончился.
 *
 * @param timeUntilNextClaim оставшееся время
 */
public record Cooldown(@NotNull Duration timeUntilNextClaim) implements DailyRewardResult {
}