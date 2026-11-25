package com.avengers.cardgame.config;

import com.avengers.cardgame.model.HeroRarity;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * Игровые настройки (кулдауны, интервалы, параметры валюты и экономики).
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
     * Возвращает экономические параметры редкости.
     *
     * @param rarity редкость
     * @return параметры редкости
     * @throws IllegalStateException если параметры редкости отсутствуют
     */
    public @NotNull RarityConfig rarity(@NotNull HeroRarity rarity) {
        RarityConfig config = rarityConfigs.get(rarity);
        if (config == null) {
            throw new IllegalStateException("Missing rarity config for " + rarity);
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
