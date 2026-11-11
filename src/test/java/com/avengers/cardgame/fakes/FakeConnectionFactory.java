package com.avengers.cardgame.fakes;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.DatabaseConfig;

import java.sql.Connection;

/**
 * Фабрика соединений для тестов: всегда возвращает один и тот же {@link FakeConnection}.
 */
public class FakeConnectionFactory extends ConnectionFactory {

    /** Соединение, которое возвращает фабрика. */
    private final FakeConnection connection;

    /**
     * @param connection фейковое соединение
     */
    public FakeConnectionFactory(FakeConnection connection) {
        super(new DatabaseConfig("jdbc:fake", "fake", "fake"));
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }
}
