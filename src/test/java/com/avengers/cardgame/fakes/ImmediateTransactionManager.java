package com.avengers.cardgame.fakes;

import com.avengers.cardgame.exception.AppException;
import com.avengers.cardgame.transaction.TransactionManager;
import com.avengers.cardgame.transaction.TransactionWork;

/**
 * Транзакционный менеджер для тестов: выполняет работу без реальной транзакции.
 * Подходит для in-memory репозиториев, которые не используют соединение.
 */
public class ImmediateTransactionManager implements TransactionManager {

    @Override
    public <T> T inTransaction(TransactionWork<T> work) throws AppException {
        return work.run(null);
    }
}