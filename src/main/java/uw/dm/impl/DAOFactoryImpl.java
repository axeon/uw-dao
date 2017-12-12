package uw.dm.impl;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uw.dm.BatchupdateManager;
import uw.dm.DAOFactory;
import uw.dm.DataEntity;
import uw.dm.DataList;
import uw.dm.DataSet;
import uw.dm.SequenceManager;
import uw.dm.TransactionException;
import uw.dm.TransactionManager;
import uw.dm.service.StatsLogService;
import uw.dm.vo.SqlExecuteStats;

/**
 * DAOFactory实现类.
 * 
 * @author axeon
 */
public class DAOFactoryImpl extends DAOFactory {

	/**
	 * 批量更新实例.
	 */
	private BatchupdateManagerImpl batchupdate;

	/**
	 * 事务处理实例.
	 */
	private TransactionManagerImpl transaction;

	/**
	 * 统计信息.
	 */
	private List<SqlExecuteStats> statsList = null;

	/**
	 * 获得一个DAOFactory的实现.
	 */
	public DAOFactoryImpl() {
		transaction = new TransactionManagerImpl();
		batchupdate = new BatchupdateManagerImpl();
	}

	/**
	 * 使用固定连接名获得一个DAOFactory实现.
	 * 
	 * @param connname
	 */
	public DAOFactoryImpl(String connname) {
		transaction = new TransactionManagerImpl(connname);
		batchupdate = new BatchupdateManagerImpl();
	}

	/**
	 * 添加性能统计数据.
	 * 
	 * @param connName
	 *            连接名称
	 * @param sql
	 *            sql
	 * @param param
	 *            sql参数
	 * @param rowNum
	 *            返回/影响的行数
	 * @param dbTime
	 *            数据库层操作数据库消耗的时间
	 * @param allTime
	 *            数据库层消耗的时间
	 * @param exception
	 *            异常信息
	 */
	void addSqlExecuteStats(String connName, String sql, String param, int rowNum, long dbTime, long allTime,
			String exception) {
		if (statsList != null) {
			statsList.add(new SqlExecuteStats(connName, sql, param, rowNum, dbTime, allTime, exception));
		}
		StatsLogService.logStats(connName, sql, param, rowNum, dbTime, allTime, exception);
	}

	/**
	 * 开始批量更新.
	 * 
	 * @return BatchupdateManager对象
	 */
	@Override
	public BatchupdateManager beginBatchupdate() throws TransactionException {
		this.batchupdate.startBatchUpdate();
		return this.batchupdate;
	}

	/**
	 * 开始处理事务.
	 * 
	 * @return TransactionManager对象
	 */
	@Override
	public TransactionManager beginTransaction() throws TransactionException {
		this.transaction.startTransaction();
		return this.transaction;
	}

	/**
	 * 根据主键删除一个Entity实例，等效于delete.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param entity
	 *            要更新的对象
	 * @param <T>
	 *            映射的类型
	 * @return int
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int delete(String connName, T entity) throws TransactionException {
		return delete(connName, entity, null);
	}

	/**
	 * 根据主键删除一个Entity实例，等效于delete.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @param <T>
	 *            映射的类型
	 * @return int
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int delete(String connName, T entity, String tableName) throws TransactionException {
		int effect = EntityCommandImpl.delete(this, connName, entity, tableName);
		return effect;
	}

	/**
	 * 根据主键删除一个Entity实例，等效于delete.
	 * 
	 * @param entity
	 *            要更新的对象
	 * @param <T>
	 *            映射的类型
	 * @return int
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int delete(T entity) throws TransactionException {
		return delete(null, entity, null);
	}

	/**
	 * 根据主键删除一个Entity实例，等效于delete.
	 * 
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @param <T>
	 *            映射的类型
	 * @return int
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int delete(T entity, String tableName) throws TransactionException {
		return delete(null, entity, tableName);
	}

	/**
	 * 关闭sql执行统计，将会影响getSqlExecuteStatsList的数据.
	 */
	@Override
	public void disableSqlExecuteStats() {
		statsList = null;
	}

	/**
	 * 打开sql执行统计，将会影响getSqlExecuteStatsList的数据.
	 */
	@Override
	public void enableSqlExecuteStats() {
		statsList = new ArrayList<SqlExecuteStats>();
	}

	/**
	 * 执行一条SQL语句.
	 * 
	 * @param sql
	 *            查询的SQL
	 * @return 影响的行数
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public int executeCommand(String sql) throws TransactionException {
		return SQLCommandImpl.executeSQL(this, null, sql, null);

	}

	/**
	 * 执行一条SQL语句.
	 * 
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 影响的行数
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public int executeCommand(String sql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.executeSQL(this, null, sql, paramList);

	}

	/**
	 * 执行一条SQL语句.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param sql
	 *            查询的SQL
	 * @return 影响的行数
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public int executeCommand(String connName, String sql) throws TransactionException {
		return SQLCommandImpl.executeSQL(this, connName, sql, null);

	}

	/**
	 * 执行一条SQL语句.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 影响的行数
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public int executeCommand(String connName, String sql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.executeSQL(this, connName, sql, paramList);

	}

	/**
	 * 获得控制器.
	 * 
	 * @return BatchupdateManagerImpl对象
	 */
	BatchupdateManagerImpl getBatchUpdateController() {
		return batchupdate;
	}

	/**
	 * 获得一个java.sql.Connection连接。 请注意，这是一个原生的Connection对象，需确保手工关闭.
	 * 
	 * @param configName
	 *            配置名
	 * @return Connection对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public Connection getConnection(String configName) throws SQLException {
		return transaction.getConnection(configName);
	}

	/**
	 * 根据表名和访问类型获得一个java.sql.Connection。 请注意，这是一个原生的Connection对象，需确保手工关闭.
	 * 
	 * @param table
	 *            表名
	 * @param access
	 *            访问类型。支持all/read/write
	 * @return Connection对象
	 * @throws SQLException
	 *             SQL异常
	 */
	@Override
	public Connection getConnection(String table, String access) throws SQLException {
		return transaction.getConnection(table, access);
	}

	/**
	 * 根据表名和访问类型获得一个数据库连接配置名.
	 * 
	 * @param table
	 *            表名
	 * @param access
	 *            访问类型。支持all/read/write
	 * @return 数据库连接配置名
	 */
	@Override
	public String getConnectionName(String table, String access) {
		return transaction.getConnName(table, access);
	}

	/**
	 * 获得当前DAOFactory实例下sql执行次数.
	 * 
	 * @return sql执行次数
	 */
	@Override
	public int getInvokeCount() {
		return transaction.getInvokeCount();
	}

	/**
	 * 根据Entity来获得seq序列。 此序列通过一个系统数据库来维护，可以保证在分布式下的可用性.
	 * 
	 * @param cls
	 *            实体类类型
	 * @return seq序列
	 */
	@Override
	public long getSequenceId(Class<?> cls) {
		String tableName = EntityCommandImpl.getTableName(cls);
		if (tableName != null) {
			return getSequenceId(tableName);
		} else {
			return -1;
		}
	}

	/**
	 * 根据表名来获得seq序列。 此序列通过一个系统数据库来维护，可以保证在分布式下的可用性.
	 * 
	 * @param tableName
	 *            表名
	 * @return seq序列
	 */
	@Override
	public long getSequenceId(String tableName) {
		long sequence = -1;
		sequence = SequenceManager.nextId(tableName);
		return sequence;
	}

	/**
	 * 获得当前DAOFactory实例下的sql执行统计列表.
	 * 
	 * @return 统计列表
	 */
	@Override
	public List<SqlExecuteStats> getSqlExecuteStatsList() {
		return statsList;
	}

	/**
	 * 获得事务控制器.
	 * 
	 * @return TransactionManagerImpl对象
	 */
	TransactionManagerImpl getTransactionController() {
		return transaction;
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql) throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, null, 0, 0, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql, int startIndex, int resultNum)
			throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, null, startIndex, resultNum, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql, int startIndex, int resultNum, boolean autoCount)
			throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, null, startIndex, resultNum, autoCount);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql, Object[] paramList) throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, paramList, 0, 0, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql, Object[] paramList, int startIndex, int resultNum)
			throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, paramList, startIndex, resultNum, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(Class<T> cls, String selectsql, Object[] paramList, int startIndex, int resultNum,
			boolean autoCount) throws TransactionException {
		return EntityCommandImpl.list(this, null, cls, selectsql, paramList, startIndex, resultNum, autoCount);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql) throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, null, 0, 0, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql, int startIndex, int resultNum)
			throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, null, startIndex, resultNum, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql, int startIndex, int resultNum,
			boolean autoCount) throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, null, startIndex, resultNum, autoCount);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param <T>
	 *            映射的类型
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql, Object[] paramList)
			throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, paramList, 0, 0, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql, Object[] paramList, int startIndex,
			int resultNum) throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, paramList, startIndex, resultNum, false);
	}

	/**
	 * 根据指定的映射类型，返回一个DataList列表.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @return DataList列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> DataList<T> list(String connName, Class<T> cls, String selectsql, Object[] paramList, int startIndex,
			int resultNum, boolean autoCount) throws TransactionException {
		return EntityCommandImpl.list(this, connName, cls, selectsql, paramList, startIndex, resultNum, autoCount);
	}

	/**
	 * 根据指定的主键ID载入一个Entity实例.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param id
	 *            主键数值
	 * @return DataList对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T load(Class<T> cls, Serializable id) throws TransactionException {
		return EntityCommandImpl.load(this, null, cls, null, id);
	}

	/**
	 * 根据指定的主键ID载入一个Entity实例.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param tableName
	 *            指定表名
	 * @param id
	 *            主键数值
	 * @return DataList对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T load(Class<T> cls, String tableName, Serializable id) throws TransactionException {
		return EntityCommandImpl.load(this, null, cls, tableName, id);
	}

	/**
	 * 根据指定的主键ID载入一个Entity实例.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param id
	 *            主键数值
	 * @return DataList对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T load(String connName, Class<T> cls, Serializable id) throws TransactionException {
		return EntityCommandImpl.load(this, connName, cls, null, id);
	}

	/**
	 * 根据指定的主键ID载入一个Entity实例.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param tableName
	 *            指定表名
	 * @param id
	 *            主键数值
	 * @return DataList对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T load(String connName, Class<T> cls, String tableName, Serializable id) throws TransactionException {
		return EntityCommandImpl.load(this, connName, cls, tableName, id);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, null, 0, 0, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql, int startIndex, int resultNum) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, null, startIndex, resultNum, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql, int startIndex, int resultNum, boolean autoCount)
			throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, null, startIndex, resultNum, autoCount);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, paramList, 0, 0, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql, Object[] paramList, int startIndex, int resultNum)
			throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, paramList, startIndex, resultNum, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String selectsql, Object[] paramList, int startIndex, int resultNum,
			boolean autoCount) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, null, selectsql, paramList, startIndex, resultNum, autoCount);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，当设置为null时候，根据sql语句或表名确定
	 * @param selectsql
	 *            查询的SQL
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, null, 0, 0, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql, int startIndex, int resultNum)
			throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, null, startIndex, resultNum, false);

	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param selectsql
	 *            查询的SQL
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql, int startIndex, int resultNum, boolean autoCount)
			throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, null, startIndex, resultNum, autoCount);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, paramList, 0, 0, false);
	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql, Object[] paramList, int startIndex, int resultNum)
			throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, paramList, startIndex, resultNum, false);

	}

	/**
	 * 返回一个DataSet数据列表。 相比较DataList列表，这不是一个强类型列表，但是更加灵活.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的绑定参数
	 * @param startIndex
	 *            开始位置，默认为0
	 * @param resultNum
	 *            结果集大小，默认为0，获取全部数据
	 * @param autoCount
	 *            是否统计全部数据（用于分页算法），默认为false。
	 * @return DataSet数据列表
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public DataSet queryForDataSet(String connName, String selectsql, Object[] paramList, int startIndex, int resultNum,
			boolean autoCount) throws TransactionException {
		return SQLCommandImpl.selectForDataSet(this, connName, selectsql, paramList, startIndex, resultNum, autoCount);
	}

	/**
	 * 查询单个基本数值列表（多行单个字段）.
	 * 
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @return DataSet对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> List<T> queryForSingleList(Class<T> cls, String sql) throws TransactionException {
		return SQLCommandImpl.selectForSingleList(this, null, cls, sql, null);
	}

	/**
	 * 查询单个基本数值列表（多行单个字段）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> List<T> queryForSingleList(String connName, Class<T> cls, String sql) throws TransactionException {
		return SQLCommandImpl.selectForSingleList(this, connName, cls, sql, null);
	}

	/**
	 * 查询单个基本数值列表（多行单个字段）.
	 * 
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> List<T> queryForSingleList(Class<T> cls, String sql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.selectForSingleList(this, null, cls, sql, paramList);
	}

	/**
	 * 查询单个基本数值列表（多行单个字段）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> List<T> queryForSingleList(String connName, Class<T> cls, String sql, Object[] paramList)
			throws TransactionException {
		return SQLCommandImpl.selectForSingleList(this, connName, cls, sql, paramList);
	}

	/**
	 * 查询单个对象（单行数据）。 使用sql中探测到的表名来决定连接名.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleObject(Class<T> cls, String selectsql) throws TransactionException {
		return EntityCommandImpl.listSingle(this, null, cls, selectsql, null);
	}

	/**
	 * 查询单个对象（单行数据）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleObject(String connName, Class<T> cls, String selectsql) throws TransactionException {
		return EntityCommandImpl.listSingle(this, connName, cls, selectsql, null);
	}

	/**
	 * 查询单个对象（单行数据）。 使用sql中探测到的表名来决定连接名.
	 * 
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleObject(Class<T> cls, String selectsql, Object[] paramList) throws TransactionException {
		return EntityCommandImpl.listSingle(this, null, cls, selectsql, paramList);
	}

	/**
	 * 查询单个对象（单行数据）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的对象类型
	 * @param <T>
	 *            映射的类型
	 * @param selectsql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleObject(String connName, Class<T> cls, String selectsql, Object[] paramList)
			throws TransactionException {
		return EntityCommandImpl.listSingle(this, connName, cls, selectsql, paramList);
	}

	/**
	 * 查询单个基本数值（单个字段）.
	 * 
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleValue(Class<T> cls, String sql) throws TransactionException {
		return SQLCommandImpl.selectForSingleValue(this, null, cls, sql, null);

	}

	/**
	 * 查询单个基本数值（单个字段）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleValue(String connName, Class<T> cls, String sql) throws TransactionException {
		return SQLCommandImpl.selectForSingleValue(this, connName, cls, sql, null);
	}

	/**
	 * 查询单个基本数值（单个字段）.
	 * 
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleValue(Class<T> cls, String sql, Object[] paramList) throws TransactionException {
		return SQLCommandImpl.selectForSingleValue(this, null, cls, sql, paramList);
	}

	/**
	 * 查询单个基本数值（单个字段）.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param cls
	 *            要映射的基础类型，如int.class,long.class,String.class,Date.class
	 * @param <T>
	 *            映射的类型
	 * @param sql
	 *            查询的SQL
	 * @param paramList
	 *            查询SQL的参数
	 * @return 单个对象
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T> T queryForSingleValue(String connName, Class<T> cls, String sql, Object[] paramList)
			throws TransactionException {
		return SQLCommandImpl.selectForSingleValue(this, connName, cls, sql, paramList);
	}

	/**
	 * 保存一个Entity实例，等效于insert.
	 * 
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param <T>
	 *            映射的类型
	 * @param entity
	 *            要更新的对象
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> T save(String connName, T entity) throws TransactionException {
		return EntityCommandImpl.save(this, connName, entity, null);
	}

	/**
	 * 保存一个Entity实例，等效于insert.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> T save(String connName, T entity, String tableName) throws TransactionException {
		return EntityCommandImpl.save(this, connName, entity, tableName);
	}

	/**
	 * 保存一个Entity实例，等效于insert.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param entity
	 *            要更新的对象
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> T save(T entity) throws TransactionException {
		return EntityCommandImpl.save(this, null, entity, null);
	}

	/**
	 * 保存一个Entity实例，等效于insert.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> T save(T entity, String tableName) throws TransactionException {
		return EntityCommandImpl.save(this, null, entity, tableName);
	}

	/**
	 * 根据主键更新一个Entity实例，等效于update.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param entity
	 *            要更新的对象
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int update(String connName, T entity) throws TransactionException {
		return EntityCommandImpl.update(this, connName, entity, null);
	}

	/**
	 * 根据主键更新一个Entity实例，等效于update.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param connName
	 *            连接名，如设置为null，则根据sql语句或表名动态路由确定
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int update(String connName, T entity, String tableName) throws TransactionException {
		return EntityCommandImpl.update(this, connName, entity, tableName);
	}

	/**
	 * 根据主键更新一个Entity实例，等效于update.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param entity
	 *            要更新的对象
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int update(T entity) throws TransactionException {
		return EntityCommandImpl.update(this, null, entity, null);
	}

	/**
	 * 根据主键更新一个Entity实例，等效于update.
	 * 
	 * @param <T>
	 *            映射的类型
	 * @param entity
	 *            要更新的对象
	 * @param tableName
	 *            指定表名
	 * @return Entity实例
	 * @throws TransactionException
	 *             事务异常
	 */
	@Override
	public <T extends DataEntity> int update(T entity, String tableName) throws TransactionException {
		return EntityCommandImpl.update(this, null, entity, tableName);

	}

}
