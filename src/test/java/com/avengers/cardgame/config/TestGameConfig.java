package com.avengers.cardgame.config;

import com.avengers.cardgame.model.HeroRarity;

import java.time.Duration;
import java.util.Map;

/**
 * Фабрика игровой конфигурации для тестов.
 */
public final class TestGameConfig {

    /** Название валюты по умолчанию. */
    public static final String CURRENCY_NAME = "Кристаллы";

    /** Эмодзи валюты по умолчанию. */
    public static final String CURRENCY_EMOJI = "💎";

    /** Эмодзи кусочков по умолчанию. */
    public static final String SHARDS_EMOJI = "🧩";

    /** Комиссия рынка по умолчанию (в процентах). */
    public static final int COMMISSION = 5;

    /**
     * Создаёт конфигурацию с параметрами по умолчанию.
     *
     * @return игровая конфигурация
     */
    public GameConfig defaultConfig() {
        return withCooldown(Duration.ofHours(24));
    }

    /**
     * Создаёт конфигурацию с заданным кулдауном.
     *
     * @param cooldown время ожидания между /daily
     * @return игровая конфигурация
     */
    public GameConfig withCooldown(Duration cooldown) {
        return new GameConfig(cooldown, Duration.ofMinutes(30), CURRENCY_NAME, CURRENCY_EMOJI,
                SHARDS_EMOJI, defaultRarities(), COMMISSION);
    }

    /**
     * Создаёт конфигурацию с заданным интервалом уведомлений.
     *
     * @param interval интервал проверки уведомлений
     * @return игровая конфигурация
     */
    public GameConfig withNotificationInterval(Duration interval) {
        return new GameConfig(Duration.ofHours(24), interval, CURRENCY_NAME, CURRENCY_EMOJI,
                SHARDS_EMOJI, defaultRarities(), COMMISSION);
    }

    /**
     * Создаёт конфигурацию с заданной комиссией рынка.
     *
     * @param commission комиссия рынка в процентах
     * @return игровая конфигурация
     */
    public GameConfig withCommission(int commission) {
        return new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), CURRENCY_NAME,
                CURRENCY_EMOJI, SHARDS_EMOJI, defaultRarities(), commission);
    }

    /**
     * Создаёт конфигурацию с заданными названием и эмодзи.
     *
     * @param currencyName  название валюты
     * @param currencyEmoji эмодзи валюты
     * @param shardsEmoji   эмодзи кусочков
     * @return игровая конфигурация
     */
    public GameConfig withCurrency(String currencyName, String currencyEmoji, String shardsEmoji) {
        return new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), currencyName,
                currencyEmoji, shardsEmoji, defaultRarities(), COMMISSION);
    }

    /**
     * Создаёт конфигурацию с заданными валютой и комиссией.
     *
     * @param currencyName  название валюты
     * @param currencyEmoji эмодзи валюты
     * @param shardsEmoji   эмодзи кусочков
     * @param commission    комиссия рынка в процентах
     * @return игровая конфигурация
     */
    public GameConfig withCurrencyAndCommission(String currencyName, String currencyEmoji,
                                                String shardsEmoji, int commission) {
        return new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), currencyName,
                currencyEmoji, shardsEmoji, defaultRarities(), commission);
    }

    /**
     * Возвращает параметры редкостей по умолчанию.
     *
     * @return параметры редкостей
     */
    public Map<HeroRarity, GameConfig.RarityConfig> defaultRarities() {
        return Map.of(
                HeroRarity.COMMON, new GameConfig.RarityConfig(1, 5, 10, 200),
                HeroRarity.RARE, new GameConfig.RarityConfig(3, 15, 50, 1000),
                HeroRarity.EPIC, new GameConfig.RarityConfig(8, 40, 200, 5000),
                HeroRarity.LEGENDARY, new GameConfig.RarityConfig(20, 100, 1000, 25000)
        );
    }
}
