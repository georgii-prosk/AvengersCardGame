package com.avengers.cardgame.presentation;

import java.time.Duration;

/**
 * Форматирует промежутки времени в читаемый вид.
 */
public class DurationFormatter {

    /** Количество минут в одном часе. */
    private static final long MINUTES_IN_HOUR = 60;

    /**
     * Форматирует промежуток времени как часы и минуты.
     * <p>
     * Неполная минута округляется вверх, промежуток меньше минуты отображается как одна минута.
     *
     * @param duration промежуток времени
     * @return строка вида "5 ч 32 мин"
     */
    public String format(Duration duration) {
        long minutes = Math.max(1, duration.plusMinutes(1).minusNanos(1).toMinutes());
        long hours = minutes / MINUTES_IN_HOUR;
        long remainingMinutes = minutes % MINUTES_IN_HOUR;

        if (hours == 0) {
            return remainingMinutes + " мин";
        }
        if (remainingMinutes == 0) {
            return hours + " ч";
        }
        return hours + " ч " + remainingMinutes + " мин";
    }
}
