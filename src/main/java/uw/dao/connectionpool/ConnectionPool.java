package uw.dao.connectionpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.conf.DaoConfig.ConnPoolConfig;
import uw.dao.conf.DaoConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认的数据库连接池.
 */
public class ConnectionPool {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	/**
	 * 连接列表.
	 */
	CopyOnWriteArrayList<ConnectionWrapper> connList = null;

	/**
	 * 连接池名.
	 */
	final String poolName;

	/**
	 * 数据库驱动.
	 */
	final String dbDriver;

	/**
	 * 数据库服务器.
	 */
	final String dbServer;

	/**
	 * 连接用户名.
	 */
	final String dbUsername;

	/**
	 * 连接密码.
	 */
	final String dbPassword;

	/**
	 * 测试sql.
	 */
	final String testSQL;

	/**
	 * 最小连接数.
	 */
	int minConns;

	/**
	 * 最大连接数.
	 */
	int maxConns;

	/**
	 * 静默超时.
	 */
	long connIdleTimeout;

	/**
	 * 忙超时.
	 */
	long connBusyTimeout;

	/**
	 * 连接寿命.
	 */
	long connMaxAge;

	/**
	 * 连接池状态.
	 */
	boolean available = false;

	/**
	 * 数据类型.
	 */
	String dbType = "";

	/**
	 * 初始化连接池.
	 * 
	 * @param poolName
	 *            连接池名字
	 */
	public ConnectionPool(String poolName) {
		this.poolName = poolName;
		ConnPoolConfig config = DaoConfigManager.getConnPoolConfig(this.poolName);
		// 驱动
		dbDriver = config.getDriver();
		// 服务器连接字符串
		dbServer = config.getUrl();
		// 登陆用户名
		dbUsername = config.getUsername();
		// 登陆密码
		dbPassword = config.getPassword();
		// 测试sql
		testSQL = config.getTestSql();
		// 最小连接数
		minConns = config.getMinConn();
		// 最大连接数
		maxConns = config.getMaxConn();
		// 空闲超时(秒钟)
		connIdleTimeout = config.getConnIdleTimeout();
		// 忙超时（秒钟）
		connBusyTimeout = config.getConnBusyTimeout();
		// 连接寿命（秒钟）
		connMaxAge = config.getConnMaxAge();
		// 数据类型
		dbType = config.getDbType();

		if (this.minConns < 1) {
			this.minConns = 1;
			config.setMinConn(minConns);
		}
		if (this.maxConns < 1) {
			this.maxConns = 1;
			config.setMaxConn(maxConns);
		}

		this.connMaxAge = connMaxAge * 1000;
		this.connIdleTimeout = connIdleTimeout * 1000;
		this.connBusyTimeout = connBusyTimeout * 1000;
		if (connIdleTimeout < 60000) {
			// 最小一分钟
			connIdleTimeout = 60000;
			config.setConnIdleTimeout(60);
		}

		if (connBusyTimeout < 30000) {
			// 最小30秒钟
			connBusyTimeout = 30000;
			config.setConnBusyTimeout(30);
		}

		if (connMaxAge < 600000) {
			// 最小10分钟
			connMaxAge = 600000;
			config.setConnMaxAge(60);
		}

		this.connList = new CopyOnWriteArrayList<ConnectionWrapper>();

		// 启动连接池
		start();
	}

	/**
	 * 启动连接池.
	 */
	public synchronized void start() {
		this.available = true;
		if (logger.isTraceEnabled()) {
			logger.trace("Starting ConnectionPool[" + poolName + "]:");
			logger.trace("dbDriver = " + dbDriver);
			logger.trace("dbServer = " + dbServer);
			logger.trace("dbLogin = " + dbUsername);
			logger.trace("minConnections = " + minConns);
			logger.trace("maxConnections = " + maxConns);
			logger.trace("connIdleTimeout = " + connIdleTimeout / 1000 + " seconds");
			logger.trace("connBusyTimeout = " + connBusyTimeout / 1000 + " seconds");
			logger.trace("connMaxAge = " + connMaxAge / 1000 + " seconds");
		}
	}

	/**
	 * 获取一个连接，原来对方法进行了同步，后来发现是一个性能问题。 现在把同步关掉，里面算法基本上是基于retry的了.
	 * 
	 * @return 一个连接
	 */
	public Connection getConnection() {
		Connection conn = null;
		if (available) {
			boolean gotOne = false;
			for (int outerloop = 0; outerloop < 200; outerloop++) {
				for (int loop = 0; loop < connList.size(); loop++) {
					ConnectionWrapper cw = null;
					try {
						// 防止get的时候刚好没有数值
						cw = connList.get(loop);
					} catch (Exception e) {
					}
					if (cw != null && cw.trySetUseStatus()) {
						if (cw.liteCheckAlive()) {
							conn = cw;
							gotOne = true;
							break;
						}
					}
				}
				if (gotOne) {
					break;
				} else if (outerloop > 1) {
					createConn("by can't get a idle connection!");
				}
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
				}
				// 超过30次找不到，报告连接耗尽信息
				if (outerloop > 30) {
					logger.warn("-----> ConnectionPool[" + poolName + "](" + connList.size()
							+ ") Exhausted!  Will wait and try again in loop " + outerloop);
				}
			}
		} else {
			logger.warn("ConnectionPool[" + poolName + "] Unsuccessful getConnection() request during destroy()");
		}

		return conn;
	}

	/**
	 * 创建一个新连接.
	 * 
	 * @param reason
	 *            原因
	 */
	void createConn(String reason) {
		if (connList.size() < maxConns) {
			try {
				Class.forName(dbDriver);
				// 获得一个封装对象
				ConnectionWrapper cw = new ConnectionWrapper(
						DriverManager.getConnection(dbServer, dbUsername, dbPassword), poolName);
				cw.setReadyStatus();
				connList.add(cw);
				if (logger.isTraceEnabled()) {
					logger.trace("ConnectionPool[" + poolName + "](" + connList.size() + ") opening connection : "
							+ cw.toString() + " " + reason);
				}
			} catch (Exception e) {
				logger.error("--->ConnectionPool[" + poolName + "](" + connList.size()
						+ ") Attempt failed to create new connection " + reason, e);
			}
		}
	}

	/**
	 * 销毁连接池.
	 */
	public void destroy() {
		// Stop issuing connections
		available = false;

		// Close all connections, whether safe or not
		for (int i = connList.size() - 1; i >= 0; i--) {
			ConnectionWrapper cw = connList.get(i);
			cw.trueClose();
			connList.remove(cw);
		}

		if (connList.size() > 0) {
			// bt-test successful
			String msg = "ConnectionPool[" + poolName + "] Unsafe shutdown: Had to close " + connList.size()
					+ " active DB connections";
			logger.error(msg);
		}

	}

}
