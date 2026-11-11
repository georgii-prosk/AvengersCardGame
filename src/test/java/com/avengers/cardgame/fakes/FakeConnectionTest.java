package com.avengers.cardgame.fakes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Тесты для {@link FakeConnection}.
 */
class FakeConnectionTest {

    @Test
    @DisplayName("По умолчанию включён автокоммит и нет транзакций")
    void defaultState() {
        try (FakeConnection connection = new FakeConnection()) {
            Assertions.assertTrue(connection.getAutoCommit());
            Assertions.assertEquals(0, connection.commitCount());
            Assertions.assertEquals(0, connection.rollbackCount());
            Assertions.assertFalse(connection.isClosed());
        }
    }

    @Test
    @DisplayName("setAutoCommit меняет режим")
    void setAutoCommitChangesMode() {
        try (FakeConnection connection = new FakeConnection()) {
            connection.setAutoCommit(false);

            Assertions.assertFalse(connection.getAutoCommit());
        }
    }

    @Test
    @DisplayName("commit увеличивает счётчик фиксаций")
    void commitIncrementsCounter() {
        try (FakeConnection connection = new FakeConnection()) {
            connection.commit();
            connection.commit();

            Assertions.assertEquals(2, connection.commitCount());
            Assertions.assertEquals(0, connection.rollbackCount());
        }
    }

    @Test
    @DisplayName("rollback увеличивает счётчик откатов")
    void rollbackIncrementsCounter() {
        try (FakeConnection connection = new FakeConnection()) {
            connection.rollback();

            Assertions.assertEquals(1, connection.rollbackCount());
            Assertions.assertEquals(0, connection.commitCount());
        }
    }

    @Test
    @DisplayName("close помечает соединение закрытым")
    void closeMarksConnectionClosed() {
        try (FakeConnection connection = new FakeConnection()) {
            connection.close();

            Assertions.assertTrue(connection.isClosed());
        }
    }

    @Test
    @DisplayName("Неподдерживаемые операции бросают исключение")
    void unsupportedOperationsThrow() {
        try (FakeConnection connection = new FakeConnection()) {
            Assertions.assertThrows(UnsupportedOperationException.class, connection::createStatement);
            Assertions.assertThrows(UnsupportedOperationException.class,
                    () -> connection.prepareStatement("SELECT 1"));
        }
    }
}
