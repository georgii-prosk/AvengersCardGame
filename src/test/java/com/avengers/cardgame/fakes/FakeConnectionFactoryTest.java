package com.avengers.cardgame.fakes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

/**
 * Тесты для {@link FakeConnectionFactory}.
 */
class FakeConnectionFactoryTest {

    @Test
    @DisplayName("Всегда возвращает одно и то же соединение")
    void alwaysReturnsSameConnection() {
        FakeConnection connection = new FakeConnection();
        FakeConnectionFactory factory = new FakeConnectionFactory(connection);

        Connection first = factory.getConnection();
        Connection second = factory.getConnection();

        Assertions.assertSame(connection, first);
        Assertions.assertSame(connection, second);
    }
}
