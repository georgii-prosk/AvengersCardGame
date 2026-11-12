package com.avengers.cardgame.fakes;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.config.DatabaseConfig;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

/**
 * Фабрика, возвращающая одно и то же {@link FakeConnection}.
 */
public class FakeConnectionFactory extends ConnectionFactory {

    /** Фейковое соединение, которое возвращает фабрика. */
    private final FakeConnection connection = new FakeConnection();

    /**
     * Создаёт фабрику с фейковыми параметрами подключения.
     */
    public FakeConnectionFactory() {
        super(new DatabaseConfig("jdbc:fake", "fake", "fake"));
    }

    @Override
    public @NotNull Connection getConnection() {
        return connection.connection();
    }

    /**
     * @return фейковое соединение
     */
    public FakeConnection connection() {
        return connection;
    }
}
