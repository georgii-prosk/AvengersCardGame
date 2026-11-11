package com.avengers.cardgame.config;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * Игровые настройки (кулдауны, интервалы, параметры валюты).
 *
 * @param dailyCooldown        время ожидания между командами /daily
 * @param notificationInterval интервал проверки планировщика уведомлений
 * @param currencyName         название основной игровой валюты
 * @param currencyEmoji        эмодзи основной игровой валюты
 */
public record GameConfig(
        Duration dailyCooldown,
        Duration notificationInterval,
        @NotNull String currencyName,
        @NotNull String currencyEmoji
) {

    /**
     * Создаёт конфигурацию с валидацией инвариантов.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     * @param currencyName         название основной валюты
     * @param currencyEmoji        эмодзи основной валюты
     * @throws IllegalArgumentException если длительности отрицательные или нулевые,
     *                                  либо название или эмодзи валюты пустые
     */
    public GameConfig {
        if (dailyCooldown == null || dailyCooldown.isNegative() || dailyCooldown.isZero()) {
            throw new IllegalArgumentException("Daily cooldown must be positive");
        }
        if (notificationInterval == null || notificationInterval.isNegative() || notificationInterval.isZero()) {
            throw new IllegalArgumentException("Notification interval must be positive");
        }
        Objects.requireNonNull(currencyName, "Currency name must not be null");
        Objects.requireNonNull(currencyEmoji, "Currency emoji must not be null");
        if (currencyName.isBlank()) {
            throw new IllegalArgumentException("Currency name must not be blank");
        }
        if (currencyEmoji.isBlank()) {
            throw new IllegalArgumentException("Currency emoji must not be blank");
        }
    }
}
