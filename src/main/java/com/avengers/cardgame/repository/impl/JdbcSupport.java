package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Базовый класс для репозиториев на основе JDBC.
 */
public abstract class JdbcSupport {

    /** Логгер. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Фабрика соединений. */
    protected final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений с БД
     */
    protected JdbcSupport(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * Возвращает переданное соединение или открывает новое.
     *
     * @param connection соединение, может быть {@code null}
     * @return соединение для выполнения запроса
     * @throws SQLException при ошибке открытия соединения
     */
    protected Connection acquireConnection(@Nullable Connection connection) throws SQLException {
        return connection != null ? connection : connectionFactory.getConnection();
    }

    /**
     * Закрывает соединение, если оно было открыто внутри репозитория.
     *
     * @param original         переданное снаружи соединение, может быть {@code null}
     * @param actualConnection фактически использованное соединение
     */
    protected void closeIfOwned(@Nullable Connection original, @Nullable Connection actualConnection) {
        if (original == null && actualConnection != null) {
            try {
                actualConnection.close();
            } catch (SQLException exception) {
                log.warn("Failed to close database connection", exception);
            }
        }
    }
}
