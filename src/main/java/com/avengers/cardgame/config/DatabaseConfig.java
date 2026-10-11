package com.avengers.cardgame.config;

/**
 * Параметры подключения к базе данных.
 *
 * @param url      JDBC URL базы данных
 * @param user     имя пользователя
 * @param password пароль
 */
public record DatabaseConfig(String url, String user, String password) {
}