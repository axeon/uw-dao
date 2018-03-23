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

/**
 * @author Brett Wooldridge
 */
public class StubStatement implements Statement {
    private static long executeDelay;
    protected int count;
    private boolean closed;

    public static void setExecuteDelayMs(final long delay) {
        executeDelay = delay;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return (T) this;
        }

        throw new SQLException("Wrapped connection is not an instance of " + iface);
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
    public ResultSet executeQuery(String sql) throws SQLException {
        if (executeDelay > 0) {
//         final long start = nanoTime();
//         do {
//            // spin
//         } while (nanoTime() - start < MILLISECONDS.toNanos(executeDelayMs));
//         UtilityElf.quietlySleep(executeDelay);
        }
        return new StubResultSet();
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(String sql) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SQLException {
        closed = true;
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxFieldSize() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxFieldSize(int max) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getMaxRows() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setMaxRows(int max) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void setEscapeProcessing(boolean enable) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getQueryTimeout() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setQueryTimeout(int seconds) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void cancel() throws SQLException {
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
    }

    /**
     * {@inheritDoc}
     */
    public void setCursorName(String name) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(String sql) throws SQLException {
        return sql.startsWith("I");
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet getResultSet() throws SQLException {
        return new StubResultSet();
    }

    /**
     * {@inheritDoc}
     */
    public int getUpdateCount() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getMoreResults() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchDirection() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchDirection(int direction) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getFetchSize() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void setFetchSize(int rows) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int getResultSetConcurrency() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int getResultSetType() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public void addBatch(String sql) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void clearBatch() throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public int[] executeBatch() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Connection getConnection() throws SQLException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public boolean getMoreResults(int current) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public ResultSet getGeneratedKeys() throws SQLException {
        return new StubResultSet();
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public int getResultSetHoldability() throws SQLException {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isClosed() throws SQLException {
        return closed;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isPoolable() throws SQLException {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void setPoolable(boolean poolable) throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public void closeOnCompletion() throws SQLException {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCloseOnCompletion() throws SQLException {
        return false;
    }

    public int getCount() {
        return count;
    }
}
