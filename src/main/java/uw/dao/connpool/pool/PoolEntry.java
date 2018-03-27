
package uw.dao.connpool.pool;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.connpool.util.ConcurrentBag;
import uw.dao.connpool.util.FastList;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static uw.dao.connpool.util.ClockSource.*;


/**
 * Connection的封装对象。
 *
 */
final class PoolEntry implements ConcurrentBag.IConcurrentBagEntry {
    private static final Logger LOGGER = LoggerFactory.getLogger(PoolEntry.class);
    private static final AtomicIntegerFieldUpdater<PoolEntry> stateUpdater;

    static {
        stateUpdater = AtomicIntegerFieldUpdater.newUpdater(PoolEntry.class, "state");
    }

    private final FastList<Statement> openStatements;
    private final HikariPool hikariPool;
    private final boolean isReadOnly;
    private final boolean isAutoCommit;
    Connection connection;
    long lastAccessed;
    long lastBorrowed;
    @SuppressWarnings("FieldCanBeLocal")
    private volatile int state = 0;
    private volatile boolean evict;
    private volatile ScheduledFuture<?> endOfLife;

    PoolEntry(final Connection connection, final PoolBase pool, final boolean isReadOnly, final boolean isAutoCommit) {
        this.connection = connection;
        this.hikariPool = (HikariPool) pool;
        this.isReadOnly = isReadOnly;
        this.isAutoCommit = isAutoCommit;
        this.lastAccessed = currentTime();
        this.openStatements = new FastList<>(Statement.class, 16);
    }

    /**
     * Release this entry back to the pool.
     *
     * @param lastAccessed last access time-stamp
     */
    void recycle(final long lastAccessed) {
        if (connection != null) {
            this.lastAccessed = lastAccessed;
            hikariPool.recycle(this);
        }
    }

    /**
     * Set the end of life {@link ScheduledFuture}.
     *
     * @param endOfLife this PoolEntry/Connection's end of life {@link ScheduledFuture}
     */
    void setFutureEol(final ScheduledFuture<?> endOfLife) {
        this.endOfLife = endOfLife;
    }

    Connection createProxyConnection(final ProxyLeakTask leakTask, final long now) {
        return ProxyFactory.getProxyConnection(this, connection, openStatements, leakTask, now, isReadOnly, isAutoCommit);
    }

    void resetConnectionState(final ProxyConnection proxyConnection, final int dirtyBits) throws SQLException {
        hikariPool.resetConnectionState(connection, proxyConnection, dirtyBits);
    }

    String getPoolName() {
        return hikariPool.toString();
    }

    boolean isMarkedEvicted() {
        return evict;
    }

    void markEvicted() {
        this.evict = true;
    }

    void evict(final String closureReason) {
        hikariPool.closeConnection(this, closureReason);
    }

    /**
     * Returns millis since lastBorrowed
     */
    long getMillisSinceBorrowed() {
        return elapsedMillis(lastBorrowed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final long now = currentTime();
        return connection
                + ", accessed " + elapsedDisplayString(lastAccessed, now) + " ago, "
                + stateToString();
    }

    // ***********************************************************************
    //                      IConcurrentBagEntry methods
    // ***********************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public int getState() {
        return stateUpdater.get(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int update) {
        stateUpdater.set(this, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean compareAndSet(int expect, int update) {
        return stateUpdater.compareAndSet(this, expect, update);
    }

    Connection close() {
        ScheduledFuture<?> eol = endOfLife;
        if (eol != null && !eol.isDone() && !eol.cancel(false)) {
            LOGGER.warn("{} - maxLifeTime expiration task cancellation unexpectedly returned false for connection {}", getPoolName(), connection);
        }

        Connection con = connection;
        connection = null;
        endOfLife = null;
        return con;
    }

    private String stateToString() {
        switch (state) {
            case STATE_IN_USE:
                return "IN_USE";
            case STATE_NOT_IN_USE:
                return "NOT_IN_USE";
            case STATE_REMOVED:
                return "REMOVED";
            case STATE_RESERVED:
                return "RESERVED";
            default:
                return "Invalid";
        }
    }
}
