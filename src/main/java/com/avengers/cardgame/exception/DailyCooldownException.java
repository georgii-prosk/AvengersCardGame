package com.avengers.cardgame.exception;

import java.time.Duration;

/**
 * Доменная ошибка: ежедневная награда ещё недоступна, кулдаун не истёк.
 */
public class DailyCooldownException extends AppException {

    /** Сколько ещё нужно подождать до следующей выдачи. */
    private final Duration timeLeft;

    /**
     * @param timeLeft оставшееся время ожидания
     */
    public DailyCooldownException(Duration timeLeft) {
        super("Daily is on cooldown, time left: " + timeLeft);
        this.timeLeft = timeLeft;
    }

    /**
     * @return оставшееся время ожидания до следующей выдачи
     */
    public Duration timeLeft() {
        return timeLeft;
    }
}
