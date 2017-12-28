package uw.dao.connectionpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Connection实现，用于封装Connection.
 */
public class ConnectionWrapper implements Connection {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConnectionWrapper.class);

	/**
	 * 创建时间.
	 */
	final long createTime = System.currentTimeMillis();
	/**
	 * 状态锁，用于防止争用.
	 */
	private final ReentrantLock statusLocker = new ReentrantLock();

	/**
	 * 开始使用时间.
	 */
	long busyTime;

	/**
	 * 开始静默时间.
	 */
	long idleTime;

	/**
	 * 连接状态。 -1：不可用 0：可用 1：使用中 2：检测中.
	 */
	private int status = -1;
	/**
	 * 实际的数据库连接.
	 */
	private final Connection connection;

	/**
	 * 获得数据库连接池名.
	 */
	private final String poolName;

	/**
	 * 异常.
	 */
	Exception ex = null;

	/**
	 * 是否追踪堆栈，默认为true.
	 */
	private final boolean TRACE = true;

	/**
	 * 构造函数.
	 * 
	 * @param connection
	 *            Connection对象
	 * @param poolName
	 *            连接池名字
	 */
	public ConnectionWrapper(Connection connection, String poolName) {
		this.connection = connection;
		this.poolName = poolName;
	}

	/**
	 * the connection to get.
	 * 
	 * @return Connection
	 */
	public Connection getSourceObject() {
		return this.connection;
	}

	/**
	 * 设定不使用的状态.
	 */
	void setUnuseStatus() {
		statusLocker.lock();
		try {
			this.status = -1;
			this.busyTime = -1;
			this.idleTime = System.currentTimeMillis();
		} finally {
			statusLocker.unlock();
		}
	}

	/**
	 * 设定为正常状态.
	 */
	void setReadyStatus() {
		statusLocker.lock();
		try {
			this.status = 0;
			this.busyTime = -1;
			this.idleTime = System.currentTimeMillis();
		} finally {
			statusLocker.unlock();
		}
	}

	/**
	 * 是否设为使用状态.
	 * 
	 * @return true或者false
	 */
	boolean trySetUseStatus() {
		boolean flag = false;
		statusLocker.lock();
		try {
			if (status == 0) {
				this.status = 1;
				this.busyTime = System.currentTimeMillis();
				this.idleTime = -1;
				if (TRACE) {
					ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date());
				}
				flag = true;
			}
		} finally {
			statusLocker.unlock();
		}
		return flag;
	}

	/**
	 * 是否设定为测试状态.
	 * 
	 * @return true或者false
	 */
	boolean trySetTestStatus() {
		boolean flag = false;
		statusLocker.lock();
		try {
			if (status == 0) {
				this.status = 2;
				flag = true;
			}
		} finally {
			statusLocker.unlock();
		}
		return flag;
	}

	/**
	 * 设定为退出测试状态，一般状态为0.
	 */
	void setTestSuccessStatus() {
		statusLocker.lock();
		this.status = 0;
		this.busyTime = -1;
		statusLocker.unlock();
	}

	/**
	 * 获得状态.
	 * 
	 * @return 连接状态的int
	 */
	int getStatus() {
		statusLocker.lock();
		int status = this.status;
		statusLocker.unlock();
		return status;
	}

	/**
	 * 真正关闭掉.
	 */
	protected void trueClose() {
		this.setUnuseStatus();
		if (connection != null) {
			try {
				if (!connection.getAutoCommit()) {
					// 没执行完的东西，一定要回滚了，防止出问题。
					try {
						connection.rollback();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
					try {
						connection.setAutoCommit(true);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			} catch (SQLException e1) {
				logger.error(e1.getMessage(), e1);
			}
			try {
				this.connection.close();
			} catch (Exception e1) {
				logger.error(e1.getMessage(), e1);
			}
		}
	}

	/**
	 * 轻量级别的检测是否存在。 true是ok.
	 * 
	 * @return true或者false
	 */
	boolean liteCheckAlive() {
		boolean flag = false;
		try {
			flag = !this.connection.isClosed();
		} catch (SQLException e) {
		}
		return flag;
	}

	/**
	 * 关闭操作， 将连接池放回.
	 * 
	 * @throws SQLException
	 *             SQL异常
	 */
	public void close() throws SQLException{
		if (connection != null) {
			try {
				if (!connection.getAutoCommit()) {
					connection.rollback();
					connection.setAutoCommit(true);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				this.trueClose();
			}
			this.setReadyStatus();
		} else {
			this.trueClose();
		}
		this.ex = null;
		idleTime = System.currentTimeMillis();
		busyTime = -1;
	}

	/**
	 * the holdability to get.
	 * 
	 * @return int
	 * @throws SQLException
	 *             SQL异常
	 */
	public int getHoldability() throws SQLException {
		return this.connection.getHoldability();
	}

	/**
	 * the holdability to set.
	 * 
	 * @param holdability
	 *            a holdability constant
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setHoldability(int holdability) throws SQLException {
		this.connection.setHoldability(holdability);
	}

	/**
	 * the savepoint to set.
	 * 
	 * @return Savepoint对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Savepoint setSavepoint() throws SQLException {
		return this.connection.setSavepoint();
	}

	/**
	 * 释放Savepoint.
	 * 
	 * @param savepoint
	 *            Savepoint对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.connection.releaseSavepoint(savepoint);
	}

	/**
	 * 取消Savepoint.
	 * 
	 * @param savepoint
	 *            Savepoint对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public void rollback(Savepoint savepoint) throws SQLException {
		this.connection.rollback(savepoint);
	}

	/**
	 * 创建Statement对象.
	 * 
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @param resultSetHoldability
	 *            ResultSet提交后是否打开
	 * @return Statement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
			throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date());
		}
		return this.connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * 创建CallableStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @param resultSetHoldability
	 *            ResultSet提交后是否打开
	 * @return CallableStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return this.connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param autoGeneratedKeys
	 *            与数据库的主键值是否绑定
	 * @return PreparedStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return this.connection.prepareStatement(sql, autoGeneratedKeys);
	}

	/**
	 * the Savepoint to set.
	 * 
	 * @param name
	 *            Savepoint名字
	 * @return Savepoint对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Savepoint setSavepoint(String name) throws SQLException {
		return this.connection.setSavepoint(name);
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param columnNames
	 *            插入的字段数组
	 * @return PreparedStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return this.connection.prepareStatement(sql, columnNames);
	}

	/**
	 * Connection的字符串形式.
	 * 
	 * @return Connection的字符串形式
	 */
	public String toString() {
		if (connection != null) {
			return connection.toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * 创建Statement对象.
	 * 
	 * @return Statement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Statement createStatement() throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date());
		}
		return connection.createStatement();
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @return PreparedStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return connection.prepareStatement(sql);
	}

	/**
	 * 创建CallableStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @return CallableStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public CallableStatement prepareCall(String sql) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return connection.prepareCall(sql);
	}

	/**
	 * 将给定的SQL语句转换为本地使用的原生SQL语句.
	 * 
	 * @param sql
	 *            SQL语句
	 * @return 本地使用的原生SQL语句
	 * @throws SQLException
	 *             SQL异常
	 */
	public String nativeSQL(String sql) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return connection.nativeSQL(sql);
	}

	/**
	 * 是否自动提交事务.
	 * 
	 * @param autoCommit
	 *            boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setAutoCommit(boolean autoCommit) throws SQLException {
		connection.setAutoCommit(autoCommit);
	}

	/**
	 * the AutoCommit to get.
	 * 
	 * @return boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public boolean getAutoCommit() throws SQLException {
		return connection.getAutoCommit();
	}

	/**
	 * 提交事务.
	 * 
	 * @throws SQLException
	 *             SQL异常
	 */
	public void commit() throws SQLException {
		connection.commit();
	}

	/**
	 * 回滚.
	 * 
	 * @throws SQLException
	 *             SQL异常
	 */
	public void rollback() throws SQLException {
		connection.rollback();
	}

	/**
	 * 查询此 Connection对象是否已经被关闭.
	 * 
	 * @return boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public boolean isClosed() {
		return connection == null;
	}

	/**
	 * 获取DatabaseMetaData对象.
	 * 
	 * @return DatabaseMetaData对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public DatabaseMetaData getMetaData() throws SQLException {
		return connection.getMetaData();
	}

	/**
	 * 设置Connection对象是否只读状态.
	 * 
	 * @param readOnly
	 *            boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setReadOnly(boolean readOnly) throws SQLException {
		connection.setReadOnly(readOnly);
	}

	/**
	 * 查询Connection对象是否是只读状态.
	 * 
	 * @return boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public boolean isReadOnly() throws SQLException {
		return connection.isReadOnly();
	}

	/**
	 * 设置给定的目录名称.
	 * 
	 * @param catalog
	 *            目录名称
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setCatalog(String catalog) throws SQLException {
		connection.setCatalog(catalog);
	}

	/**
	 * the Catalog to set.
	 * 
	 * @return 目录名称
	 * @throws SQLException
	 *             SQL异常
	 */
	public String getCatalog() throws SQLException {
		return connection.getCatalog();
	}

	/**
	 * 设置事务隔离级别.
	 * 
	 * @param level
	 *            int
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setTransactionIsolation(int level) throws SQLException {
		connection.setTransactionIsolation(level);
	}

	/**
	 * 获取事务隔离级别.
	 * 
	 * @return int
	 * @throws SQLException
	 *             SQL异常
	 */
	public int getTransactionIsolation() throws SQLException {
		return connection.getTransactionIsolation();
	}

	/**
	 * 获取SQL警告.
	 * 
	 * @return SQL警告
	 * @throws SQLException
	 *             SQL异常
	 */
	public SQLWarning getWarnings() throws SQLException {
		return connection.getWarnings();
	}

	/**
	 * 清除SQL警告.
	 * 
	 * @throws SQLException
	 *             SQL异常
	 */
	public void clearWarnings() throws SQLException {
		connection.clearWarnings();
	}

	/**
	 * 创建Statement对象.
	 * 
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @return Statement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date());
		}
		return connection.createStatement(resultSetType, resultSetConcurrency);
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @return Statement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
			throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * 创建CallableStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @return CallableStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return connection.prepareCall(sql, resultSetType, resultSetConcurrency);
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param resultSetType
	 *            ResultSet对象的类型标示是否可滚动
	 * @param resultSetConcurrency
	 *            ResultSet对象是否能够修改
	 * @param resultSetHoldability
	 *            ResultSet提交后是否打开
	 * @return Statement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
			int resultSetHoldability) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return this.connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
	}

	/**
	 * 创建PreparedStatement对象.
	 * 
	 * @param sql
	 *            SQL语句
	 * @param columnIndexes
	 *            插入的字段的索引数组
	 * @return PreparedStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
		if (TRACE) {
			ex = new Exception("Connection[" + poolName + "] busy timeout! TIME:" + new Date() + " SQL:" + sql);
		}
		return this.connection.prepareStatement(sql, columnIndexes);
	}

	/**
	 * 检索与此Connection对象相关的Map对象.
	 * 
	 * @return Map对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getTypeMap() throws SQLException {
		return connection.getTypeMap();
	}

	/**
	 * 对象数组的元素映射到指定的SQL类型.
	 * 
	 * @param arg0
	 *            SQL类型
	 * @param arg1
	 *            对象数组
	 * @return 对象数组的元素映射到指定的SQL类型
	 * @throws SQLException
	 *             SQL异常
	 */
	public Array createArrayOf(String arg0, Object[] arg1) throws SQLException {
		return connection.createArrayOf(arg0, arg1);
	}

	/**
	 * 创建Blob对象.
	 * 
	 * @return Blob对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Blob createBlob() throws SQLException {
		return connection.createBlob();
	}

	/**
	 * 创建Clob对象.
	 * 
	 * @return Clob对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Clob createClob() throws SQLException {
		return connection.createClob();
	}

	/**
	 * 创建NClob对象.
	 * 
	 * @return NClob对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public NClob createNClob() throws SQLException {
		return connection.createNClob();
	}

	/**
	 * 创建SQLXML对象.
	 * 
	 * @return SQLXML对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public SQLXML createSQLXML() throws SQLException {
		return connection.createSQLXML();
	}

	/**
	 * 映射到给定SQL类型并填充给定属性的结构对象.
	 * 
	 * @param arg0
	 *            SQL类型
	 * @param arg1
	 *            属性的Object数组
	 * @return Struct对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Struct createStruct(String arg0, Object[] arg1) throws SQLException {
		return connection.createStruct(arg0, arg1);
	}

	/**
	 * 获取客户端信息属性集.
	 * 
	 * @return Properties对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public Properties getClientInfo() throws SQLException {
		return connection.getClientInfo();
	}

	/**
	 * 获取的客户端信息属性的名称.
	 * 
	 * @param arg0
	 *            属性的名称
	 * @return 属性的值
	 * @throws SQLException
	 *             SQL异常
	 */
	public String getClientInfo(String arg0) throws SQLException {
		return connection.getClientInfo(arg0);
	}

	/**
	 * 检查Connection是否有效.
	 * 
	 * @param arg0
	 *            用于检查的超时时间
	 * @return boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public boolean isValid(int arg0) throws SQLException {
		return connection.isValid(arg0);
	}

	/**
	 * the ClientInfo to set.
	 * 
	 * @param arg0
	 *            Properties对象
	 * @throws SQLClientInfoException
	 *             SQL异常
	 */
	public void setClientInfo(Properties arg0) throws SQLClientInfoException {
		connection.setClientInfo(arg0);
	}

	/**
	 * the ClientInfo to set.
	 * 
	 * @param arg0
	 *            属性的名称
	 * @param arg1
	 *            属性的值
	 * @throws SQLClientInfoException
	 *             SQL异常
	 */
	public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException {
		connection.setClientInfo(arg0, arg1);
	}

	/**
	 * the TypeMap to set.
	 * 
	 * @param arg0
	 *            Map对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		connection.setTypeMap(arg0);
	}

	/**
	 * 如果Connection调用此方法实现接口参数，或者是实现接口参数的对象的直接或间接包装器，则返回 true.
	 * 
	 * @param arg0
	 *            定义接口的类
	 * @return boolean
	 * @throws SQLException
	 *             SQL异常
	 */
	public boolean isWrapperFor(Class<?> arg0) throws SQLException {
		return connection.isWrapperFor(arg0);
	}

	/**
	 * 实现接口的对象.
	 * 
	 * @param arg0
	 *            定义结果必须实现的接口的类
	 * @param <T>
	 *            对象的类型
	 * @return 实现接口的对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public <T> T unwrap(Class<T> arg0) throws SQLException {
		return connection.unwrap(arg0);
	}

	/**
	 * 中止一个打开的Connection.
	 * 
	 * @param executor
	 *            Executor对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public void abort(Executor executor) throws SQLException {
		connection.abort(executor);

	}

	/**
	 * 获取Connection对象的超时限制，毫秒为单位，零代表无限制.
	 * 
	 * @return int
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public int getNetworkTimeout() throws SQLException {
		return connection.getNetworkTimeout();
	}

	/**
	 * 获取Schema（模式）对象.
	 * 
	 * @return Schema对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public String getSchema() throws SQLException {
		return connection.getSchema();
	}

	/**
	 * 设置Connection对象的超时限制.
	 * 
	 * @param executor
	 *            Executor对象
	 * @param milliseconds
	 *            超时时间
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
		connection.setNetworkTimeout(executor, milliseconds);
	}

	/**
	 * the Schema to set.
	 * 
	 * @param schema
	 *            Schema对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public void setSchema(String schema) throws SQLException {
		connection.setSchema(schema);
	}

}