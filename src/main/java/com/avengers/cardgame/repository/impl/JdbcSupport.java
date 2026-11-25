package com.avengers.cardgame.repository.impl;

import com.avengers.cardgame.config.ConnectionFactory;
import com.avengers.cardgame.exception.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Базовый класс для репозиториев на основе JDBC.
 */
public abstract class JdbcSupport {

    /** Текст технической ошибки при работе с БД. */
    private static final String TRANSACTION_ERROR_MESSAGE = "Error executing transaction";

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
     * Выполняет работу в рамках одной транзакции.
     * <p>
     * При доменной или непредвиденной ошибке транзакция откатывается,
     * соединение закрывается в любом случае.
     *
     * @param work работа, выполняемая в рамках транзакции
     * @param <T>  тип результата работы
     * @return результат работы
     * @throws AppException если работа завершилась доменной ошибкой
     */
    protected <T> T inTransaction(TransactionWork<T> work) throws AppException {
        Connection connection = null;
        try {
            connection = connectionFactory.getConnection();
            connection.setAutoCommit(false);
            T result;
            try {
                result = work.execute(connection);
            } catch (AppException | RuntimeException exception) {
                rollback(connection, exception);
                throw exception;
            } catch (SQLException exception) {
                rollback(connection, exception);
                throw new RuntimeException(TRANSACTION_ERROR_MESSAGE, exception);
            }
            connection.commit();
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
    private void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            log.error("Failed to roll back transaction", rollbackException);
            cause.addSuppressed(rollbackException);
        }
    }

    /**
     * Закрывает соединение и записывает ошибку закрытия в лог.
     *
     * @param connection соединение с БД, может быть {@code null}
     */
    private void closeQuietly(Connection connection) {
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
    protected interface TransactionWork<T> {

        /**
         * Выполняет работу, используя переданное соединение.
         *
         * @param connection соединение с БД внутри транзакции
         * @return результат работы
         * @throws AppException при доменной ошибке
         * @throws SQLException при ошибке работы с БД
         */
        T execute(Connection connection) throws AppException, SQLException;
    }
}
