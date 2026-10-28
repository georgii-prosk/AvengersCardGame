package com.avengers.cardgame.config;

/** Конфигурация приложения. */
public record AppConfig(String botToken, DatabaseConfig database, GameConfig game) {
}
