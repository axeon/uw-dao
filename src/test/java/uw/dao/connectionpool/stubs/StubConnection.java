/*
 * Copyright (C) 2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uw.dao.connectionpool.stubs;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Brett Wooldridge
 */
public class StubConnection implements Connection {
    public static volatile boolean throwRandomExceptions;

    private static long foo;

    static {
        foo = System.currentTimeMillis();
    }

    private boolean autoCommit;
    private boolean isClosed;
    private int isolation;

    /**
     * {@inheritDoc}
     */
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Statement createStatement() throws SQLException {
        return new StubStatement();
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String nativeSQL(String sql) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getAutoCommit() throws SQLException {
        return autoCommit;
    }

    /**
     * {@inheritDoc}
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        this.autoCommit = autoCommit;
    }

    /**
     * {@inheritDoc}
     */
    public void commit() throws SQLException {

    }

    /**
     * {@inheritDoc}
     */
    public void rollback() throws SQLException {
        autoCommit = false;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        isClosed = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    /**
     * {@inheritDoc}
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadOnly() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public String getCatalog() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setCatalog(String catalog) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getTransactionIsolation() throws SQLException {
        return isolation;
    }

    /**
     * {@inheritDoc}
     */
    public void setTransactionIsolation(int level) throws SQLException {
        this.isolation = level;
    }

    /**
     * {@inheritDoc}
     */
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void clearWarnings() throws SQLException {
        autoCommit = false;
    }

    /**
     * {@inheritDoc}
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getHoldability() throws SQLException {
        return (int) foo;
    }

    /**
     * {@inheritDoc}
     */
    public void setHoldability(int holdability) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void rollback(Savepoint savepoint) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return new StubPreparedStatement();
    }

    /**
     * {@inheritDoc}
     */
    public Clob createClob() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Blob createBlob() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public NClob createNClob() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public SQLXML createSQLXML() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isValid(int timeout) throws SQLException {
        if (throwRandomExceptions && ThreadLocalRandom.current().nextInt(100) == 9) {
            throw new RuntimeException("isValidThrowsException=true");
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
    }

    /**
     * {@inheritDoc}
     */
    public String getClientInfo(String name) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Properties getClientInfo() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
    }

    /**
     * {@inheritDoc}
     */
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getSchema() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setSchema(String schema) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void abort(Executor executor) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getNetworkTimeout() throws SQLException {
        return 0;
    }

}