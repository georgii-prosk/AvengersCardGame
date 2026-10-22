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

    // Можешь добавить тесты на отрицательные значения
}