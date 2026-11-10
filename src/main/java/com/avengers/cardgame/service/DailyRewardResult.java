package com.avengers.cardgame.service;

/**
 * Результат выдачи ежедневной награды.
 */
public sealed interface DailyRewardResult permits Cooldown, Success {
}