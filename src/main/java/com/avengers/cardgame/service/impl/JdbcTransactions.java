package com.avengers.cardgame.service.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Выполняет операции в рамках локальной JDBC-транзакции.
 */
public final class JdbcTransactions {

    /** Текст технической ошибки при работе с БД. */
    private static final String TRANSACTION_ERROR_MESSAGE = "Error executing transaction";

    /** Логгер. */
    private static final Logger log = LoggerFactory.getLogger(JdbcTransactions.class);

    private JdbcTransactions() {
    }

    /**
     * Выполняет работу в рамках транзакции.
     * <p>
     * При ошибке транзакция откатывается, соединение закрывается в любом случае.
     *
     * @param connectionFactory фабрика соединений
     * @param work              работа, выполняемая в рамках транзакции
     * @param <T>               тип результата работы
     * @return результат работы
     * @throws AppException если работа завершилась доменной ошибкой
     */
    public static <T> T execute(ConnectionFactory connectionFactory, Work<T> work) throws AppException {
        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);
            T result;
            try {
                result = work.execute(connection);
                connection.commit();
            } catch (AppException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            }
            return result;
        } catch (SQLException exception) {
            throw new RuntimeException(TRANSACTION_ERROR_MESSAGE, exception);
        } finally {
            closeQuietly(connection);
        }
    }

    /**
     * Откатывает транзакцию и логирует ошибку отката.
     *
     * @param connection соединение с БД
     * @param cause      причина отката
     */
    private static void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            log.error("Failed to roll back transaction", rollbackException);
            cause.addSuppressed(rollbackException);
        }
    }

    /**
     * Закрывает соединение, логируя ошибку закрытия.
     *
     * @param connection соединение с БД, может быть {@code null}
     */
    private static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            log.warn("Failed to close database connection", exception);
        }
    }

    /**
     * Работа, выполняемая в рамках транзакции.
     *
     * @param <T> тип результата работы
     */
    @FunctionalInterface
    public interface Work<T> {

        /**
         * Выполняет работу, используя переданное соединение.
         *
         * @param connection соединение с БД внутри транзакции
         * @return результат работы
         * @throws AppException при доменной ошибке
         */
        T execute(Connection connection) throws AppException;
    }
}
