package com.avengers.cardgame.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private final String url;
    private final String user;
    private final String password;

    /**
     * Рабочий конструктор. Читает параметры из переменных окружения.
     */
    public DatabaseConfig() {
        this(System.getenv("DB_URL"),
                System.getenv("DB_USER"),
                System.getenv("DB_PASSWORD"));
    }

    /**
     * Конструктор для тестов. Позволяет передать параметры напрямую
     * (например, из Testcontainers).
     *
     * @param url      JDBC URL базы данных
     * @param user     имя пользователя
     * @param password пароль
     */
    public DatabaseConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Открывает соединение с базой данных.
     *
     * @return новое соединение {@link Connection}
     * @throws SQLException при ошибке подключения
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}