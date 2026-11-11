package com.avengers.cardgame.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Открывает соединения с базой данных.
 */
public class ConnectionFactory {

    /** Параметры подключения к базе данных. */
    private final DatabaseConfig databaseConfiguration;


    /**
     * @param databaseConfiguration параметры подключения
     */
    public ConnectionFactory(DatabaseConfig databaseConfiguration) {
        this.databaseConfiguration = databaseConfiguration;
    }

    /**
     * Открывает новое соединение с базой данных.
     *
     * @return новое соединение
     * @throws SQLException при ошибке подключения
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseConfiguration.url(), databaseConfiguration.user(),
                databaseConfiguration.password());
    }
}