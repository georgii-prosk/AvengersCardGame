package com.avengers.cardgame.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

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
    @SuppressWarnings("DataFlowIssue")
    @DisplayName("Бросает исключение при null вместо эмодзи кусочков")
    void throwsOnNullShardsEmoji() {
        Assertions.assertThrows(NullPointerException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30),
                        "Кристаллы", "💎", null));
    }
}