package com.avengers.cardgame.config;

/**
 * Конфигурация приложения.
 *
 * @param botToken токен Telegram-бота
 * @param database параметры подключения к базе данных
 */
public record AppConfig(String botToken, DatabaseConfig database) {
}