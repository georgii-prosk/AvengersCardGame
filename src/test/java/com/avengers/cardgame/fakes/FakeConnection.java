package com.avengers.cardgame.fakes;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Фейковое соединение с БД: запоминает вызовы commit и rollback.
 * <p>
 * Интерфейс {@link Connection} реализован динамическим прокси JDK,
 * поддерживающим только операции, нужные тестам.
 */
public class FakeConnection {

    /** Методы соединения, которые фейк не поддерживает. */
    private static final Set<String> UNSUPPORTED_METHODS = Set.of(
            "createStatement", "prepareStatement", "prepareCall", "getMetaData",
            "setSavepoint", "createClob", "createBlob", "createNClob", "createSQLXML",
            "createArrayOf", "createStruct");

    /** Соединение, реализованное динамическим прокси. */
    private final Connection connection;

    /** Количество успешных вызовов commit. */
    private int commitCount;

    /** Количество вызовов rollback. */
    private int rollbackCount;

    /** Текущее значение автокоммита. */
    private boolean autoCommit = true;

    /** Признак закрытия соединения. */
    private boolean closed;

    /**
     * Создаёт фейковое соединение.
     */
    public FakeConnection() {
        this.connection = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                this::handle);
    }

    /**
     * @return соединение для передачи в сервисы и репозитории
     */
    public Connection connection() {
        return connection;
    }

    /**
     * @return количество вызовов commit
     */
    public int commitCount() {
        return commitCount;
    }

    /**
     * @return количество вызовов rollback
     */
    public int rollbackCount() {
        return rollbackCount;
    }

    /**
     * @return признак закрытия соединения
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return текущее значение автокоммита
     */
    public boolean getAutoCommit() {
        return autoCommit;
    }

    /**
     * Обрабатывает вызов методов соединения.
     *
     * @param proxy     прокси соединения
     * @param method    вызванный метод
     * @param arguments аргументы вызова
     * @return результат вызова
     * @throws Throwable если метод завершился ошибкой
     */
    private Object handle(Object proxy, Method method, Object[] arguments) throws Throwable {
        switch (method.getName()) {
            case "commit" -> {
                commitCount++;
                return null;
            }
            case "rollback" -> {
                rollbackCount++;
                return null;
            }
            case "setAutoCommit" -> {
                autoCommit = (Boolean) arguments[0];
                return null;
            }
            case "getAutoCommit" -> {
                return autoCommit;
            }
            case "close", "abort" -> {
                closed = true;
                return null;
            }
            case "isClosed" -> {
                return closed;
            }
            case "isValid" -> {
                return !closed;
            }
            case "nativeSQL" -> {
                return arguments[0];
            }
            case "unwrap" -> {
                Class<?> interfaceType = (Class<?>) arguments[0];
                if (interfaceType.isInstance(proxy)) {
                    return proxy;
                }
                throw new SQLException("FakeConnection is not a wrapper for " + interfaceType.getName());
            }
            case "isWrapperFor" -> {
                Class<?> interfaceType = (Class<?>) arguments[0];
                return interfaceType.isInstance(proxy);
            }
            case "getTransactionIsolation" -> {
                return Connection.TRANSACTION_READ_COMMITTED;
            }
            case "getHoldability" -> {
                return ResultSet.HOLD_CURSORS_OVER_COMMIT;
            }
            case "getTypeMap" -> {
                return Map.of();
            }
            case "getClientInfo" -> {
                return arguments == null || arguments.length == 0 ? new Properties() : null;
            }
            case "equals" -> {
                return proxy == arguments[0];
            }
            case "hashCode" -> {
                return System.identityHashCode(proxy);
            }
            case "toString" -> {
                return "FakeConnection";
            }
            default -> {
                if (UNSUPPORTED_METHODS.contains(method.getName())) {
                    throw new UnsupportedOperationException("Not supported by FakeConnection");
                }
                return defaultValue(method.getReturnType());
            }
        }
    }

    /**
     * Возвращает значение по умолчанию для типа результата метода.
     *
     * @param returnType тип результата метода
     * @return значение по умолчанию
     * @throws Throwable если значение не удалось получить
     */
    private static Object defaultValue(Class<?> returnType) throws Throwable {
        if (!returnType.isPrimitive() || returnType == void.class) {
            return null;
        }
        return MethodHandles.zero(returnType).invoke();
    }
}
