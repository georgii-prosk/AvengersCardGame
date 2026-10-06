package com.avengers.cardgame.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Конфигурация подключения к базе данных.
 * <p>
 * Параметры подключения берутся из переменных окружения:
 * <ul>
 *     <li>{@code DB_URL} — JDBC URL базы данных;</li>
 *     <li>{@code DB_USER} — имя пользователя;</li>
 *     <li>{@code DB_PASSWORD} — пароль.</li>
 * </ul>
 */
public class DatabaseConfig {

    /** JDBC URL базы данных из переменной окружения {@code DB_URL}. */
    private final String url = System.getenv("DB_URL");

    /** Имя пользователя БД из переменной окружения {@code DB_USER}. */
    private final String user = System.getenv("DB_USER");

    /** Пароль БД из переменной окружения {@code DB_PASSWORD}. */
    private final String password = System.getenv("DB_PASSWORD");

    /**
     * Создаёт новое подключение к базе данных.
     *
     * @return объект {@link Connection}
     * @throws SQLException если подключение не удалось установить
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}