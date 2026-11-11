package com.avengers.cardgame.presentation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * Тесты для {@link DurationFormatter}.
 */
class DurationFormatterTest {

    /** Форматтер промежутков времени. */
    private final DurationFormatter formatter = new DurationFormatter();

    /**
     * Проверяет форматирование часов и минут.
     */
    @Test
    @DisplayName("Форматирует часы и минуты")
    void formatsHoursAndMinutes() {
        Assertions.assertEquals("5 ч 32 мин", formatter.format(Duration.ofHours(5).plusMinutes(32)));
    }

    /**
     * Проверяет округление неполной минуты вверх.
     */
    @Test
    @DisplayName("Округляет неполную минуту вверх")
    void roundsPartialMinuteUp() {
        Assertions.assertEquals("5 ч 32 мин",
                formatter.format(Duration.ofHours(5).plusMinutes(31).plusSeconds(20)));
    }

    /**
     * Проверяет, что целые часы выводятся без минут.
     */
    @Test
    @DisplayName("Выводит целые часы без минут")
    void formatsWholeHours() {
        Assertions.assertEquals("2 ч", formatter.format(Duration.ofHours(2)));
    }

    /**
     * Проверяет форматирование одних минут.
     */
    @Test
    @DisplayName("Форматирует одни минуты")
    void formatsMinutesOnly() {
        Assertions.assertEquals("30 мин", formatter.format(Duration.ofMinutes(30)));
    }

    /**
     * Проверяет, что промежуток меньше минуты отображается как одна минута.
     */
    @Test
    @DisplayName("Показывает одну минуту вместо меньшего промежутка")
    void showsOneMinuteForShortDuration() {
        Assertions.assertEquals("1 мин", formatter.format(Duration.ofSeconds(20)));
    }
}
