package uw.dao.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uw.dao.conf.DMConfigManager;
import uw.dao.conf.DMConfig.ConnPoolConfig;

/**
 * 数据库连接管理器.
 */
public class ConnectionManager {

	/**
	 * 连接池缓存表.
	 */
	private static Map<String, ConnectionPool> poolMap = new ConcurrentHashMap<String, ConnectionPool>();

	/**
	 * 启动连接管理器.
	 */
	public static void start() {
		List<String> connList = DMConfigManager.getConnPoolNameList();
		for (String conn : connList) {
			initConnectionPool(conn);
		}
		ConnectionPoolMonitor.start();
	}

	/**
	 * 关闭连接管理器.
	 */
	public static void stop() {
		destroyAllConnectionPool();
	}

	/**
	 * 获得一个connection，在poolList中排名第一的为默认连接.
	 * 
	 * @return Connection
	 * @throws SQLException
	 *             SQL异常
	 */
	public static Connection getConnection() throws SQLException {
		return getConnection("");
	}

	/**
	 * 获得连接池的集合.
	 * 
	 * @return 连接池的集合
	 */
	static HashSet<ConnectionPool> getConnectionPoolSet() {
		HashSet<ConnectionPool> set = new HashSet<ConnectionPool>();
		set.addAll(poolMap.values());
		return set;
	}

	/**
	 * 获得一个新连接.
	 * 
	 * @param poolName
	 *            连接池名称
	 * @return 指定的新连接
	 * @throws SQLException
	 *             SQL异常
	 */
	public static Connection getConnection(String poolName) throws SQLException {
		// long start = System.currentTimeMillis();
		if (poolName == null) {
			poolName = "";
		}
		Connection conn = null;
		ConnectionPool connpool = getConnectionPool(poolName);
		if (connpool == null) {
			connpool = initConnectionPool(poolName);
		}
		if (connpool == null) {
			throw new SQLException("ConnectionManager.getConnection() failed to init connPool[" + poolName + "]");
		}

		conn = connpool.getConnection();
		if (conn == null) {
			throw new SQLException(
					"ConnectionManager.getConnection() failed to obtain a connection in connPool[" + poolName + "]");
		}
		// logger.error("***连接池{}耗费时间为{}ms",poolName,(System.currentTimeMillis()-start));
		return conn;
	}

	/**
	 * 销毁一个连接池.
	 * 
	 * @param poolName
	 *            连接池名字
	 */
	private static synchronized void destroyConnectionPool(String poolName) {
		ConnectionPool cp = poolMap.get(poolName);
		if (cp != null) {
			poolMap.remove(poolName);
			cp.destroy();
		}
	}

	/**
	 * 销毁全部连接池.
	 */
	private static synchronized void destroyAllConnectionPool() {
		ConnectionPoolMonitor.stop();
		for (String poolName : poolMap.keySet()) {
			destroyConnectionPool(poolName);
		}
	}

	/**
	 * 获得连接池.
	 * 
	 * @param poolName
	 *            连接池名字
	 * @return Connection
	 */
	private static ConnectionPool getConnectionPool(String poolName) {
		return poolMap.get(poolName);
	}

	/**
	 * 初始化连接池.
	 * 
	 * @param poolName
	 *            连接池名字
	 * @return Connection
	 */
	private static synchronized ConnectionPool initConnectionPool(String poolName) {
		ConnectionPool connpool = getConnectionPool(poolName);
		if (connpool == null) {
			ConnPoolConfig config = DMConfigManager.getConnPoolConfig(poolName);
			if (config != null) {
				connpool = new ConnectionPool(poolName);
				poolMap.put(poolName, connpool);
			}
		}
		return connpool;
	}

}
