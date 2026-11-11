package com.avengers.cardgame.config;

import com.avengers.cardgame.support.NullParameterAssertion;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Тесты для {@link GameConfig}.
 */
class GameConfigTest {

    /** Название валюты, используемое в тестах. */
    private static final String CURRENCY_NAME = "Кристаллы";

    /** Эмодзи валюты, используемое в тестах. */
    private static final String CURRENCY_EMOJI = "💎";

    /**
     * Создаёт конфигурацию с заданными длительностями и валютой по умолчанию.
     *
     * @param dailyCooldown        кулдаун ежедневной награды
     * @param notificationInterval интервал уведомлений
     * @return конфигурация
     */
    private GameConfig createConfiguration(Duration dailyCooldown, Duration notificationInterval) {
        return new GameConfig(dailyCooldown, notificationInterval, CURRENCY_NAME, CURRENCY_EMOJI);
    }

    @Test
    @DisplayName("Создаёт валидную конфигурацию")
    void createsValidConfig() {
        GameConfig configuration = createConfiguration(Duration.ofHours(24), Duration.ofMinutes(30));

        Assertions.assertEquals(Duration.ofHours(24), configuration.dailyCooldown());
        Assertions.assertEquals(Duration.ofMinutes(30), configuration.notificationInterval());
        Assertions.assertEquals(CURRENCY_NAME, configuration.currencyName());
        Assertions.assertEquals(CURRENCY_EMOJI, configuration.currencyEmoji());
    }

    @Test
    @DisplayName("Бросает исключение при нулевом кулдауне")
    void throwsOnZeroCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> createConfiguration(Duration.ZERO, Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Бросает исключение при отрицательном кулдауне")
    void throwsOnNegativeCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> createConfiguration(Duration.ofMinutes(-1), Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Бросает исключение при нулевом интервале уведомлений")
    void throwsOnZeroNotificationInterval() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> createConfiguration(Duration.ofHours(24), Duration.ZERO));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо кулдауна")
    void throwsOnNullCooldown() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> createConfiguration(null, Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Бросает исключение при пустом названии валюты")
    void throwsOnBlankCurrencyName() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), "  ", CURRENCY_EMOJI));
    }

    @Test
    @DisplayName("Бросает исключение при пустом эмодзи валюты")
    void throwsOnBlankCurrencyEmoji() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), CURRENCY_NAME, "  "));
    }

    @Test
    @DisplayName("Бросает исключение при null вместо названия валюты")
    @SuppressWarnings("DataFlowIssue")
    void throwsOnNullCurrencyName() {
        NullParameterAssertion.assertRejected(
                () -> new GameConfig(Duration.ofHours(24), Duration.ofMinutes(30), null, CURRENCY_EMOJI));
    }
}
