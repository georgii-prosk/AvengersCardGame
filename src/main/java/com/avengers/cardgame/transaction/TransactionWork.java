package com.avengers.cardgame.transaction;

import com.avengers.cardgame.exception.AppException;

import java.sql.Connection;

/**
 * Работа, выполняемая внутри транзакции.
 *
 * @param <T> тип результата
 */
@FunctionalInterface
public interface TransactionWork<T> {

    /**
     * Выполняет работу в рамках транзакции.
     *
     * @param connection соединение с БД, привязанное к транзакции
     * @return результат
     * @throws AppException при доменной ошибке
     */
    T run(Connection connection) throws AppException;
}