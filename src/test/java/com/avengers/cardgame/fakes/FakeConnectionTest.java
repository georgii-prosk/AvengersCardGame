package com.avengers.cardgame.fakes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Тесты для {@link FakeConnection}.
 */
class FakeConnectionTest {

    /**
     * Проверяет, что commit увеличивает счётчик.
     */
    @Test
    @DisplayName("Считает вызовы commit")
    void countsCommits() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            connection.commit();
            connection.commit();
        }

        Assertions.assertEquals(2, fakeConnection.commitCount());
        Assertions.assertEquals(0, fakeConnection.rollbackCount());
    }

    /**
     * Проверяет, что rollback увеличивает счётчик.
     */
    @Test
    @DisplayName("Считает вызовы rollback")
    void countsRollbacks() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            connection.rollback();
            connection.rollback();
            connection.rollback();
        }

        Assertions.assertEquals(3, fakeConnection.rollbackCount());
        Assertions.assertEquals(0, fakeConnection.commitCount());
    }

    /**
     * Проверяет, что откат к точке сохранения тоже считается.
     */
    @Test
    @DisplayName("Считает rollback к точке сохранения")
    void countsSavepointRollback() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            connection.rollback(null);
        }

        Assertions.assertEquals(1, fakeConnection.rollbackCount());
    }

    /**
     * Проверяет переключение режима автокоммита.
     */
    @Test
    @DisplayName("Запоминает режим автокоммита")
    void remembersAutoCommit() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            Assertions.assertTrue(fakeConnection.getAutoCommit());
            connection.setAutoCommit(false);
            Assertions.assertFalse(fakeConnection.getAutoCommit());
        }
    }

    /**
     * Проверяет, что закрытие меняет состояние соединения.
     */
    @Test
    @DisplayName("Запоминает закрытие соединения")
    void remembersClose() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            Assertions.assertTrue(connection.isValid(1));
            connection.close();

            Assertions.assertTrue(fakeConnection.isClosed());
            Assertions.assertFalse(connection.isValid(1));
        }
    }

    /**
     * Проверяет, что соединение может быть развёрнуто как само себе.
     */
    @Test
    @DisplayName("Поддерживает unwrap и isWrapperFor")
    void supportsUnwrap() throws SQLException {
        FakeConnection fakeConnection = new FakeConnection();

        try (Connection connection = fakeConnection.connection()) {
            Assertions.assertTrue(connection.isWrapperFor(Connection.class));
            Assertions.assertSame(connection, connection.unwrap(Connection.class));
            Assertions.assertThrows(SQLException.class, () -> connection.unwrap(String.class));
        }
    }
}
