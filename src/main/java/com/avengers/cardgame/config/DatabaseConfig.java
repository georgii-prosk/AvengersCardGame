package com.avengers.cardgame.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {

    private final String url = System.getenv("DB_URL");
    private final String user = System.getenv("DB_USER");
    private final String password = System.getenv("DB_PASSWORD");

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}