package com.avengers.cardgame.transaction.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.transaction.TransactionManager;
import com.avengers.cardgame.transaction.TransactionWork;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Реализация {@link TransactionManager} на основе JDBC.
 */
public class PostgresTransactionManager implements TransactionManager {

    /** Фабрика соединений. */
    private final ConnectionFactory connectionFactory;

    /**
     * @param connectionFactory фабрика соединений
     */
    public PostgresTransactionManager(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public <T> T inTransaction(TransactionWork<T> work) throws AppException {
        try (Connection conn = connectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                T result = work.run(conn);
                conn.commit();
                return result;
            } catch (AppException | RuntimeException e) {
                rollbackQuietly(conn);
                throw e;
            } catch (Exception e) {
                rollbackQuietly(conn);
                throw new RuntimeException("Transaction failed", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to run transaction", e);
        }
    }

    /**
     * Откатывает транзакцию, подавляя ошибки отката.
     *
     * @param conn соединение
     */
    private void rollbackQuietly(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            // уже в обработке исключения — вторичная ошибка не важна
        }
    }
}