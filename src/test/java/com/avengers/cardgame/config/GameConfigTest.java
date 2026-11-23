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
        GameConfig config = new GameConfig(
                Duration.ofHours(24),
                Duration.ofMinutes(30)
        );

        Assertions.assertEquals(Duration.ofHours(24), config.dailyCooldown());
        Assertions.assertEquals(Duration.ofMinutes(30), config.notificationInterval());
    }

    @Test
    @DisplayName("Бросает исключение при нулевом кулдауне")
    void throwsOnZeroCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ZERO, Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном кулдауне")
    void throwsOnNegativeCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofMinutes(-1), Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Бросает исключение при нулевом интервале уведомлений")
    void throwsOnZeroNotificationInterval() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ZERO));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо кулдауна")
    void throwsOnNullCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(null, Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Использует валюту по умолчанию в коротком конструкторе")
    void usesDefaultCurrencyInShortConstructor() {
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

        Assertions.assertEquals(GameConfig.DEFAULT_CURRENCY_NAME, config.currencyName());
        Assertions.assertEquals(GameConfig.DEFAULT_CURRENCY_EMOJI, config.currencyEmoji());
    }

    @Test
    @DisplayName("Использует эмодзи кусочков по умолчанию в коротком конструкторе")
    void usesDefaultShardsEmojiInShortConstructor() {
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

        Assertions.assertEquals(GameConfig.DEFAULT_SHARDS_EMOJI, config.shardsEmoji());
    }

    @Test
    @DisplayName("Сохраняет заданные название и эмодзи валюты")
    void keepsCustomCurrency() {
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                "Гири", "🪙");

        Assertions.assertEquals("Гири", config.currencyName());
        Assertions.assertEquals("🪙", config.currencyEmoji());
    }

    @Test
    @DisplayName("Сохраняет заданный эмодзи кусочков")
    void keepsCustomShardsEmoji() {
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                "Гири", "🪙", "⭐");

        Assertions.assertEquals("⭐", config.shardsEmoji());
    }

    @Test
    @DisplayName("Бросает исключение при пустом названии валюты")
    void throwsOnBlankCurrencyName() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "  ", "💎"));
    }

    @Test
    @DisplayName("Бросает исключение при пустом эмодзи валюты")
    void throwsOnBlankCurrencyEmoji() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "Кристаллы", " "));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо названия валюты")
    void throwsOnNullCurrencyName() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), null, "💎"));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо эмодзи валюты")
    void throwsOnNullCurrencyEmoji() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "Кристаллы", null));
    }

    @Test
    @DisplayName("Бросает исключение при пустом эмодзи кусочков")
    void throwsOnBlankShardsEmoji() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        "Кристаллы", "💎", " "));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо эмодзи кусочков")
    void throwsOnNullShardsEmoji() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        "Кристаллы", "💎", null));
    }

    @Test
    @DisplayName("Использует параметры экономики по умолчанию")
    void usesDefaultEconomyParams() {
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30));

        Assertions.assertEquals(GameConfig.DEFAULT_MARKET_COMMISSION_PERCENT, config.marketCommissionPercent());
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
        GameConfig config = new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                GameConfig.DEFAULT_CURRENCY_NAME, GameConfig.DEFAULT_CURRENCY_EMOJI,
                GameConfig.DEFAULT_SHARDS_EMOJI, new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30)).rarityConfigs(), 10);

        Assertions.assertEquals(10, config.marketCommissionPercent());
    }

    @Test
    @DisplayName("Бросает исключение при комиссии вне диапазона 0..100")
    void throwsOnCommissionOutOfRange() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        GameConfig.DEFAULT_CURRENCY_NAME, GameConfig.DEFAULT_CURRENCY_EMOJI,
                        GameConfig.DEFAULT_SHARDS_EMOJI, new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30)).rarityConfigs(), 101));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        GameConfig.DEFAULT_CURRENCY_NAME, GameConfig.DEFAULT_CURRENCY_EMOJI,
                        GameConfig.DEFAULT_SHARDS_EMOJI, new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30)).rarityConfigs(), -1));
    }

    @Test
    @DisplayName("Бросает исключение при отсутствии параметров редкости")
    void throwsOnMissingRarityConfig() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        GameConfig.DEFAULT_CURRENCY_NAME, GameConfig.DEFAULT_CURRENCY_EMOJI,
                        GameConfig.DEFAULT_SHARDS_EMOJI, Map.of(), 5));
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