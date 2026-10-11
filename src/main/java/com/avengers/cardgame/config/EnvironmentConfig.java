package com.avengers.cardgame.config;

/**
 * Конфигурация приложения, считанная из переменных окружения.
 *
 * @param botToken токен Telegram-бота
 * @param database параметры подключения к базе данных
 */
public record EnvironmentConfig(String botToken, DatabaseConfig database) {
}