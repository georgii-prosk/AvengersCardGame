package com.avengers.cardgame.config;

/**
 * Корневая конфигурация приложения.
 *
 * @param botToken токен Telegram-бота
 * @param database параметры подключения к базе данных
 * @param game     игровые настройки (кулдауны, интервалы планировщика)
 */
public record AppConfig(String botToken, DatabaseConfig database, GameConfig game) {
}