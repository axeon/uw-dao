package uw.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.TransactionException;
import uw.dao.TransactionManager;
import uw.dao.conf.DaoConfigManager;
import uw.dao.connectionpool.ConnectionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * TransactionManager实现类.
 */
public class TransactionManagerImpl implements TransactionManager {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(TransactionManagerImpl.class);

    /**
     * 是否自动提交事务.
     */
    private boolean autoCommit = true;

    /**
     * Isolation.
     */
    private int isolation = TransactionManager.TRANSACTION_READ_COMMITTED;

    /**
     * 数据库连接的map表.
     */
    private HashMap<String, Connection> connmap = null;

    /**
     * 指定的数据库联接，一旦使用，dao.config将无效.
     */
    private String specifyConnName = null;

    /**
     * 被调用个次数.
     */
    private int invokeCount;

    /**
     * 默认构造器,只能在本包内调用.
     */
    protected TransactionManagerImpl() {

    }

    /**
     * 默认构造器,只能在本包内调用.
     */
    protected TransactionManagerImpl(String specifyConnName) {
        this.specifyConnName = specifyConnName;
    }

    /**
     * 提交该事务。 将所有的Connection全部提交.
     *
     * @throws TransactionException 事务异常
     */
    @Override
    public void commit() throws TransactionException {
        Connection conn = null;
        boolean exception = false;
        if (connmap == null) {
            return;
        }
        String[] connNames = {};
        connNames = connmap.keySet().toArray(connNames);
        for (String configName : connNames) {
            conn = (Connection) connmap.get(configName);
            try {
                conn.commit();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                exception = true;
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    conn.close();
                    connmap.remove(configName);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if (exception) {
            throw new TransactionException("TransactionException in DbTransactionManager.java:commit()");
        }

        this.autoCommit = true;
        connmap = null;
    }

    /**
     * 根据连接名获得配置名称.
     *
     * @param configName 配置名称
     * @return Connection对象
     * @throws SQLException SQL语句
     */
    public Connection getConnection(String configName) throws SQLException {
        invokeCount++;
        if (specifyConnName != null) {
            configName = specifyConnName;
        }
        Connection conn = null;
        if (autoCommit) {
            conn = ConnectionManager.getConnection(configName);
        } else {
            if (connmap.containsKey(configName)) {
                conn = (Connection) connmap.get(configName);
            } else {
                conn = ConnectionManager.getConnection(configName);
                conn.setAutoCommit(false);
                // conn.setTransactionIsolation(this.isolation);
                connmap.put(configName, conn);
            }
        }

        return conn;
    }

    /**
     * factory类通过该方法为所有的数据库操作提供连接.
     *
     * @param table  表名
     * @param access 访问方式
     * @return Connection对象
     * @throws SQLException SQL异常
     */
    public Connection getConnection(String table, String access) throws SQLException {
        return this.getConnection(getConnName(table, access));
    }

    /**
     * 获得连接名.
     *
     * @param table  表名
     * @param access 访问方式
     * @return 连接名
     */
    public String getConnName(String table, String access) {
        return DaoConfigManager.getRouteMapping(table, access);
    }

    /**
     * 获取当前事务级别.
     *
     * @return 事务级别
     * @throws TransactionException 事务异常
     */
    @Override
    public int getTransactionIsolation() {
        return this.isolation;
    }

    /**
     * 设置事务级别.
     *
     * @param level 级别
     * @throws TransactionException 事务异常
     */
    @Override
    public void setTransactionIsolation(int level) {
        this.isolation = level;
    }

    /**
     * 获得当前是否是自动提交状态.
     *
     * @return boolean
     */
    @Override
    public boolean isAutoCommit() {
        return autoCommit;
    }

    /**
     * 回滚该事务,对使用的Connection统一回滚.
     *
     * @throws TransactionException 事务异常
     */
    @Override
    public void rollback() throws TransactionException {
        Connection conn = null;
        boolean exception = false;
        if (connmap == null) {
            return;
        }
        String[] connNames = {};
        connNames = connmap.keySet().toArray(connNames);
        for (String configName : connNames) {
            conn = (Connection) connmap.get(configName);
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                exception = true;
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    conn.close();
                    connmap.remove(configName);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        if (exception) {
            throw new TransactionException("TransactionException in DbTransactionManager.java:rollback()");
        }
        this.autoCommit = true;
        connmap = null;
    }

    /**
     * 当调用次方法的时候，自动设置开始transaction.
     */
    protected void startTransaction() {
        this.autoCommit = false;
        connmap = new HashMap<String, Connection>();
    }

    /**
     * 获得被调用次数.
     *
     * @return 被调用次数
     */
    protected int getInvokeCount() {
        return invokeCount;
    }

}
