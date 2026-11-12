package com.avengers.cardgame.config;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;

/**
 * Игровые настройки (кулдауны, интервалы и параметры валюты).
 *
 * @param dailyCooldown        время ожидания между командами /daily
 * @param notificationInterval интервал проверки планировщика уведомлений
 * @param currencyName         название игровой валюты
 * @param currencyEmoji        эмодзи игровой валюты
 * @param shardsEmoji          эмодзи игровых кусочков
 */
public record GameConfig(
        Duration dailyCooldown,
        Duration notificationInterval,
        @NotNull String currencyName,
        @NotNull String currencyEmoji,
        @NotNull String shardsEmoji
) {

    /** Название валюты по умолчанию. */
    public static final String DEFAULT_CURRENCY_NAME = "Кристаллы";

    /** Эмодзи валюты по умолчанию. */
    public static final String DEFAULT_CURRENCY_EMOJI = "💎";

    /** Эмодзи кусочков по умолчанию. */
    public static final String DEFAULT_SHARDS_EMOJI = "🧩";

    /**
     * Создаёт конфигурацию с валидацией инвариантов.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     * @param currencyName         название валюты
     * @param currencyEmoji        эмодзи валюты
     * @param shardsEmoji          эмодзи кусочков
     * @throws IllegalArgumentException если длительности отрицательные или нулевые,
     *                                  либо название, эмодзи валюты или эмодзи кусочков пустые
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
        Objects.requireNonNull(shardsEmoji, "Shards emoji must not be null");
        if (currencyName.isBlank()) {
            throw new IllegalArgumentException("Currency name must not be blank");
        }
        if (currencyEmoji.isBlank()) {
            throw new IllegalArgumentException("Currency emoji must not be blank");
        }
        if (shardsEmoji.isBlank()) {
            throw new IllegalArgumentException("Shards emoji must not be blank");
        }
    }

    /**
     * Создаёт конфигурацию с валютой и кусочками по умолчанию.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     */
    public GameConfig(Duration dailyCooldown, Duration notificationInterval) {
        this(dailyCooldown, notificationInterval, DEFAULT_CURRENCY_NAME, DEFAULT_CURRENCY_EMOJI,
                DEFAULT_SHARDS_EMOJI);
    }

    /**
     * Создаёт конфигурацию с заданной валютой и кусочками по умолчанию.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     * @param currencyName         название валюты
     * @param currencyEmoji        эмодзи валюты
     */
    public GameConfig(Duration dailyCooldown, Duration notificationInterval,
                      String currencyName, String currencyEmoji) {
        this(dailyCooldown, notificationInterval, currencyName, currencyEmoji, DEFAULT_SHARDS_EMOJI);
    }
}
