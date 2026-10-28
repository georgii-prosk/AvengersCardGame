package com.avengers.cardgame.service;

import com.avengers.cardgame.model.Hero;
import java.time.Duration;
import java.util.List;

public record DailyRewardResult(List<Hero> heroes, Duration timeUntilNextClaim) {
    public DailyRewardResult { heroes = List.copyOf(heroes); }
    public boolean claimed() { return timeUntilNextClaim == null; }
    public static DailyRewardResult success(List<Hero> heroes) { return new DailyRewardResult(heroes, null); }
    public static DailyRewardResult cooldown(Duration timeLeft) { return new DailyRewardResult(List.of(), timeLeft); }
}
