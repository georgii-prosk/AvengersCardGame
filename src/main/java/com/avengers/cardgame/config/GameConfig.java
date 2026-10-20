package com.avengers.cardgame.config;

import java.time.Duration;

/**
 * Игровые настройки (кулдауны, интервалы).
 *
 * @param dailyCooldown        время ожидания между командами /daily
 * @param notificationInterval интервал проверки планировщика уведомлений
 */
public record GameConfig(Duration dailyCooldown, Duration notificationInterval) {

    /**
     * Создаёт конфигурацию с валидацией инвариантов.
     *
     * @param dailyCooldown        время ожидания между /daily
     * @param notificationInterval интервал планировщика
     * @throws IllegalArgumentException если длительности отрицательные или нулевые
     */
    public GameConfig {
        if (dailyCooldown.isNegative() || dailyCooldown.isZero()) {
            throw new IllegalArgumentException("Daily cooldown must be positive");
        }
        if (notificationInterval.isNegative() || notificationInterval.isZero()) {
            throw new IllegalArgumentException("Notification interval must be positive");
        }
    }
}