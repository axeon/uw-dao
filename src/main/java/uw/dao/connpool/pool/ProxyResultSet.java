package uw.dao.connpool.pool;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This is the proxy class for java.sql.ResultSet.
 *
 * @author Brett Wooldridge
 */
public abstract class ProxyResultSet implements ResultSet {
    protected final ProxyConnection connection;
    protected final ProxyStatement statement;
    final ResultSet delegate;

    protected ProxyResultSet(ProxyConnection connection, ProxyStatement statement, ResultSet resultSet) {
        this.connection = connection;
        this.statement = statement;
        this.delegate = resultSet;
    }

    @SuppressWarnings("unused")
    final SQLException checkException(SQLException e) {
        return connection.checkException(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '@' + System.identityHashCode(this) + " wrapping " + delegate;
    }

    // **********************************************************************
    //                 Overridden java.sql.ResultSet Methods
    // **********************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
    public final Statement getStatement() throws SQLException {
        return statement;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateRow() throws SQLException {
        connection.markCommitStateDirty();
        delegate.updateRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertRow() throws SQLException {
        connection.markCommitStateDirty();
        delegate.insertRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteRow() throws SQLException {
        connection.markCommitStateDirty();
        delegate.deleteRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public final <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(delegate)) {
            return (T) delegate;
        } else if (delegate != null) {
            return delegate.unwrap(iface);
        }

        throw new SQLException("Wrapped ResultSet is not an instance of " + iface);
    }
}
