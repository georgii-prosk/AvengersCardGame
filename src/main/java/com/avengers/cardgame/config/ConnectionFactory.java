package com.avengers.cardgame.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Открывает соединения с базой данных.
 */
public class ConnectionFactory {

    private final DatabaseConfig config;

    public ConnectionFactory(DatabaseConfig config) {
        this.config = config;
    }

    /**
     * Открывает новое соединение с базой данных.
     *
     * @return новое соединение
     * @throws SQLException при ошибке подключения
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.url(), config.user(), config.password());
    }
}