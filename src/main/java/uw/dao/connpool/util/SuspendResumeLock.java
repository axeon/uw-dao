package uw.dao.connpool.util;

import java.sql.SQLException;
import java.sql.SQLTransientException;
import java.util.concurrent.Semaphore;

/**
 * This class implements a lock that can be used to suspend and resume the pool.  It
 * also provides a faux implementation that is used when the feature is disabled that
 * hopefully gets fully "optimized away" by the JIT.
 *
 * @author Brett Wooldridge
 */
public class SuspendResumeLock {
    public static final SuspendResumeLock FAUX_LOCK = new SuspendResumeLock(false) {
        @Override
        public void acquire() {
        }

        @Override
        public void release() {
        }

        @Override
        public void suspend() {
        }

        @Override
        public void resume() {
        }
    };

    private static final int MAX_PERMITS = 10000;
    private final Semaphore acquisitionSemaphore;

    /**
     * Default constructor
     */
    public SuspendResumeLock() {
        this(true);
    }

    private SuspendResumeLock(final boolean createSemaphore) {
        acquisitionSemaphore = (createSemaphore ? new Semaphore(MAX_PERMITS, true) : null);
    }

    public void acquire() throws SQLException {
        if (acquisitionSemaphore.tryAcquire()) {
            return;
        } else if (Boolean.getBoolean("com.zaxxer.hikari.throwIfSuspended")) {
            throw new SQLTransientException("The pool is currently suspended and configured to throw exceptions upon acquisition");
        }

        acquisitionSemaphore.acquireUninterruptibly();
    }

    public void release() {
        acquisitionSemaphore.release();
    }

    public void suspend() {
        acquisitionSemaphore.acquireUninterruptibly(MAX_PERMITS);
    }

    public void resume() {
        acquisitionSemaphore.release(MAX_PERMITS);
    }
}
