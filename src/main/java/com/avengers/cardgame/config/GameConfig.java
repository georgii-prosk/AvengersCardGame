package com.avengers.cardgame.config;

import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Игровые настройки (кулдауны, интервалы и параметры валюты).
 *
 * @param dailyCooldown           время ожидания между командами /daily
 * @param notificationInterval    интервал проверки планировщика уведомлений
 * @param currencyName            название игровой валюты
 * @param currencyEmoji           эмодзи игровой валюты
 * @param shardsEmoji             эмодзи игровых кусочков
 * @param rarityConfigs           экономические параметры редкостей
 * @param marketCommissionPercent комиссия рынка в процентах
 */
public record GameConfig(
        Duration dailyCooldown,
        Duration notificationInterval,
        @NotNull String currencyName,
        @NotNull String currencyEmoji,
        @NotNull String shardsEmoji,
        @NotNull Map<HeroRarity, RarityConfig> rarityConfigs,
        int marketCommissionPercent
) {

    /** Название валюты по умолчанию. */
    public static final String DEFAULT_CURRENCY_NAME = "Кристаллы";

    /** Эмодзи валюты по умолчанию. */
    public static final String DEFAULT_CURRENCY_EMOJI = "💎";

    /** Эмодзи кусочков по умолчанию. */
    public static final String DEFAULT_SHARDS_EMOJI = "🧩";

    /** Комиссия рынка по умолчанию (в процентах). */
    public static final int DEFAULT_MARKET_COMMISSION_PERCENT = 5;

    /**
     * Создаёт конфигурацию с валидацией инвариантов.
     *
     * @param dailyCooldown           время ожидания между /daily
     * @param notificationInterval    интервал планировщика
     * @param currencyName            название валюты
     * @param currencyEmoji           эмодзи валюты
     * @param shardsEmoji             эмодзи кусочков
     * @param rarityConfigs           экономические параметры редкостей
     * @param marketCommissionPercent комиссия рынка в процентах
     * @throws IllegalArgumentException если длительности отрицательные или нулевые,
     *                                  название или эмодзи пустые, не заданы параметры
     *                                  какой-либо редкости либо комиссия вне диапазона 0..100
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
        Objects.requireNonNull(rarityConfigs, "Rarity configs must not be null");
        for (HeroRarity rarity : HeroRarity.values()) {
            if (!rarityConfigs.containsKey(rarity)) {
                throw new IllegalArgumentException("Missing rarity config for " + rarity);
            }
        }
        if (marketCommissionPercent < 0 || marketCommissionPercent > 100) {
            throw new IllegalArgumentException("Market commission percent must be between 0 and 100");
        }
    }

    /**
     * Создаёт конфигурацию с валютой, кусочками и экономикой по умолчанию.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     */
    public GameConfig(Duration dailyCooldown, Duration notificationInterval) {
        this(dailyCooldown, notificationInterval, DEFAULT_CURRENCY_NAME, DEFAULT_CURRENCY_EMOJI,
                DEFAULT_SHARDS_EMOJI);
    }

    /**
     * Создаёт конфигурацию с заданной валютой и экономикой по умолчанию.
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

    /**
     * Создаёт конфигурацию с заданной валютой, кусочками и экономикой по умолчанию.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     * @param currencyName         название валюты
     * @param currencyEmoji        эмодзи валюты
     * @param shardsEmoji          эмодзи кусочков
     */
    public GameConfig(Duration dailyCooldown, Duration notificationInterval,
                      String currencyName, String currencyEmoji, String shardsEmoji) {
        this(dailyCooldown, notificationInterval, currencyName, currencyEmoji, shardsEmoji,
                DEFAULT_MARKET_COMMISSION_PERCENT);
    }

    /**
     * Создаёт конфигурацию с заданной валютой, кусочками, комиссией рынка
     * и экономикой редкостей по умолчанию.
     *
     * @param dailyCooldown           время ожидания между /daily
     * @param notificationInterval    интервал планировщика
     * @param currencyName            название валюты
     * @param currencyEmoji           эмодзи валюты
     * @param shardsEmoji             эмодзи кусочков
     * @param marketCommissionPercent комиссия рынка в процентах
     */
    public GameConfig(Duration dailyCooldown, Duration notificationInterval,
                      String currencyName, String currencyEmoji, String shardsEmoji,
                      int marketCommissionPercent) {
        this(dailyCooldown, notificationInterval, currencyName, currencyEmoji, shardsEmoji,
                Map.of(
                        HeroRarity.COMMON, new RarityConfig(1, 5, 10, 200),
                        HeroRarity.RARE, new RarityConfig(3, 15, 50, 1000),
                        HeroRarity.EPIC, new RarityConfig(8, 40, 200, 5000),
                        HeroRarity.LEGENDARY, new RarityConfig(20, 100, 1000, 25000)
                ),
                marketCommissionPercent);
    }

    /**
     * Возвращает экономические параметры редкости.
     *
     * @param rarity редкость
     * @return параметры редкости
     */
    public @NotNull RarityConfig rarity(@NotNull HeroRarity rarity) {
        RarityConfig config = rarityConfigs.get(rarity);
        if (config == null) {
            throw new IllegalArgumentException("Missing rarity config for " + rarity);
        }
        return config;
    }

    /**
     * Экономические параметры редкости.
     *
     * @param shardsYield количество кусочков, получаемых за разбор одной карточки
     * @param craftCost   количество кусочков, необходимое для крафта одной карточки
     * @param minPrice    минимально допустимая цена лота
     * @param maxPrice    максимально допустимая цена лота
     */
    public record RarityConfig(int shardsYield, int craftCost, int minPrice, int maxPrice) {

        /**
         * Проверяет корректность параметров редкости.
         *
         * @throws IllegalArgumentException если количество кусочков или цены неположительные,
         *                                  либо максимальная цена меньше минимальной
         */
        public RarityConfig {
            if (shardsYield <= 0) {
                throw new IllegalArgumentException("Shards yield must be positive");
            }
            if (craftCost <= 0) {
                throw new IllegalArgumentException("Craft cost must be positive");
            }
            if (minPrice <= 0) {
                throw new IllegalArgumentException("Minimum price must be positive");
            }
            if (maxPrice < minPrice) {
                throw new IllegalArgumentException("Maximum price must not be less than minimum price");
            }
        }
    }
}