package uw.dao.connpool.pool;

import java.sql.CallableStatement;

/**
 * This is the proxy class for java.sql.CallableStatement.
 *
 * @author Brett Wooldridge
 */
public abstract class ProxyCallableStatement extends ProxyPreparedStatement implements CallableStatement {
    protected ProxyCallableStatement(ProxyConnection connection, CallableStatement statement) {
        super(connection, statement);
    }

    // **********************************************************************
    //               Overridden java.sql.CallableStatement Methods
    // **********************************************************************

}