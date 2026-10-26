package com.avengers.cardgame.transaction;

import com.avengers.cardgame.exception.AppException;

/**
 * Выполняет работу в транзакции БД.
 */
public interface TransactionManager {

    /**
     * Открывает транзакцию, выполняет {@code work} и коммитит результат.
     * При ошибке — откатывает и пробрасывает исключение дальше.
     *
     * @param work работа
     * @param <T>  тип результата
     * @return результат работы
     * @throws AppException если работа завершилась доменной ошибкой
     */
    <T> T inTransaction(TransactionWork<T> work) throws AppException;
}