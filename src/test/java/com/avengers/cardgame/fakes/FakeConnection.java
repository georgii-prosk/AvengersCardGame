package com.avengers.cardgame.fakes;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Фейковое соединение с БД для тестов: отслеживает commit, rollback и закрытие.
 * <p>
 * Все операции, не связанные с управлением транзакцией, не поддерживаются,
 * поскольку тесты используют репозитории в памяти.
 */
public class FakeConnection implements Connection {

    /** Количество вызовов {@link #commit()}. */
    private int commitCount;

    /** Количество вызовов {@link #rollback()}. */
    private int rollbackCount;

    /** Текущий режим автокоммита. */
    private boolean autoCommit = true;

    /** Признак закрытия соединения. */
    private boolean closed;

    @Override
    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    @Override
    public boolean getAutoCommit() {
        return autoCommit;
    }

    @Override
    public void commit() {
        commitCount++;
    }

    @Override
    public void rollback() {
        rollbackCount++;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    /**
     * @return количество успешных фиксаций транзакции
     */
    public int commitCount() {
        return commitCount;
    }

    /**
     * @return количество откатов транзакции
     */
    public int rollbackCount() {
        return rollbackCount;
    }

    @Override
    public Statement createStatement() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String query) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public CallableStatement prepareCall(String query) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public String nativeSQL(String query) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public DatabaseMetaData getMetaData() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public boolean isReadOnly() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setCatalog(String catalog) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public String getCatalog() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setTransactionIsolation(int level) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public int getTransactionIsolation() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public SQLWarning getWarnings() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void clearWarnings() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String query, int resultSetType, int resultSetConcurrency)
            {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public CallableStatement prepareCall(String query, int resultSetType, int resultSetConcurrency)
            {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Map<String, Class<?>> getTypeMap() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setHoldability(int holdability) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public int getHoldability() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Savepoint setSavepoint() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Savepoint setSavepoint(String name) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void rollback(Savepoint savepoint) {
        rollbackCount++;
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(
            String query, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public CallableStatement prepareCall(
            String query, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String query, int autoGeneratedKeys) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String query, int[] columnIndexes) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public PreparedStatement prepareStatement(String query, String[] columnNames) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Clob createClob() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Blob createBlob() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public NClob createNClob() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public SQLXML createSQLXML() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public boolean isValid(int timeout) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setClientInfo(String name, String value) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setClientInfo(Properties properties) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public String getClientInfo(String name) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Properties getClientInfo() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setSchema(String schema) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public String getSchema() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void abort(Executor executor) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public int getNetworkTimeout() {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new UnsupportedOperationException("Not supported by FakeConnection");
    }
}
