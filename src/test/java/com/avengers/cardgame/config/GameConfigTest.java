package com.avengers.cardgame.config;

import com.avengers.cardgame.model.HeroRarity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Map;

/**
 * Тесты для {@link GameConfig}.
 */
class GameConfigTest {

    @Test
    @DisplayName("Создаёт валидную конфигурацию")
    void createsValidConfig() {
        GameConfig config = new TestGameConfig().defaultConfig();

        Assertions.assertEquals(Duration.ofHours(24), config.dailyCooldown());
        Assertions.assertEquals(Duration.ofMinutes(30), config.notificationInterval());
    }

    @Test
    @DisplayName("Бросает исключение при нулевом кулдауне")
    void throwsOnZeroCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCooldown(Duration.ZERO));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном кулдауне")
    void throwsOnNegativeCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCooldown(Duration.ofMinutes(-1)));
    }

    @Test
    @DisplayName("Бросает исключение при нулевом интервале уведомлений")
    void throwsOnZeroNotificationInterval() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withNotificationInterval(Duration.ZERO));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо кулдауна")
    void throwsOnNullCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCooldown(null));
    }

    @Test
    @DisplayName("Сохраняет заданные название и эмодзи валюты")
    void keepsCustomCurrency() {
        GameConfig config = new TestGameConfig()
                .withCurrency("Гири", "🪙", TestGameConfig.SHARDS_EMOJI);

        Assertions.assertEquals("Гири", config.currencyName());
        Assertions.assertEquals("🪙", config.currencyEmoji());
    }

    @Test
    @DisplayName("Сохраняет заданный эмодзи кусочков")
    void keepsCustomShardsEmoji() {
        GameConfig config = new TestGameConfig()
                .withCurrency("Гири", "🪙", "⭐");

        Assertions.assertEquals("⭐", config.shardsEmoji());
    }

    @Test
    @DisplayName("Бросает исключение при пустом названии валюты")
    void throwsOnBlankCurrencyName() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCurrency("  ", TestGameConfig.CURRENCY_EMOJI,
                        TestGameConfig.SHARDS_EMOJI));
    }

    @Test
    @DisplayName("Бросает исключение при пустом эмодзи валюты")
    void throwsOnBlankCurrencyEmoji() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCurrency(TestGameConfig.CURRENCY_NAME, " ",
                        TestGameConfig.SHARDS_EMOJI));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо названия валюты")
    void throwsOnNullCurrencyName() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new TestGameConfig().withCurrency(null, TestGameConfig.CURRENCY_EMOJI,
                        TestGameConfig.SHARDS_EMOJI));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо эмодзи валюты")
    void throwsOnNullCurrencyEmoji() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new TestGameConfig().withCurrency(TestGameConfig.CURRENCY_NAME, null,
                        TestGameConfig.SHARDS_EMOJI));
    }

    @Test
    @DisplayName("Бросает исключение при пустом эмодзи кусочков")
    void throwsOnBlankShardsEmoji() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCurrency(TestGameConfig.CURRENCY_NAME,
                        TestGameConfig.CURRENCY_EMOJI, " "));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо эмодзи кусочков")
    void throwsOnNullShardsEmoji() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new TestGameConfig().withCurrency(TestGameConfig.CURRENCY_NAME,
                        TestGameConfig.CURRENCY_EMOJI, null));
    }

    @Test
    @DisplayName("Использует параметры экономики по умолчанию")
    void usesDefaultEconomyParams() {
        GameConfig config = new TestGameConfig().defaultConfig();

        Assertions.assertEquals(TestGameConfig.COMMISSION, config.marketCommissionPercent());
        Assertions.assertEquals(1, config.rarity(HeroRarity.COMMON).shardsYield());
        Assertions.assertEquals(5, config.rarity(HeroRarity.COMMON).craftCost());
        Assertions.assertEquals(10, config.rarity(HeroRarity.COMMON).minPrice());
        Assertions.assertEquals(200, config.rarity(HeroRarity.COMMON).maxPrice());
        Assertions.assertEquals(20, config.rarity(HeroRarity.LEGENDARY).shardsYield());
        Assertions.assertEquals(100, config.rarity(HeroRarity.LEGENDARY).craftCost());
        Assertions.assertEquals(1000, config.rarity(HeroRarity.LEGENDARY).minPrice());
        Assertions.assertEquals(25000, config.rarity(HeroRarity.LEGENDARY).maxPrice());
    }

    @Test
    @DisplayName("Сохраняет заданную комиссию рынка")
    void keepsCustomCommission() {
        GameConfig config = new TestGameConfig().withCommission(10);

        Assertions.assertEquals(10, config.marketCommissionPercent());
    }

    @Test
    @DisplayName("Бросает исключение при комиссии вне диапазона 0..100")
    void throwsOnCommissionOutOfRange() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCommission(101));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new TestGameConfig().withCommission(-1));
    }

    @Test
    @DisplayName("Бросает исключение при отсутствии параметров редкости")
    void throwsOnMissingRarityConfig() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        TestGameConfig.CURRENCY_NAME, TestGameConfig.CURRENCY_EMOJI,
                        TestGameConfig.SHARDS_EMOJI, Map.of(), TestGameConfig.COMMISSION));
    }

    @Test
    @DisplayName("Бросает исключение при некорректных параметрах редкости")
    void throwsOnInvalidRarityConfig() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig.RarityConfig(0, 5, 10, 200));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig.RarityConfig(1, 5, 200, 10));
    }
}
