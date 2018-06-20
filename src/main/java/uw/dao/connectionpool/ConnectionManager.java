package uw.dao.connectionpool;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.conf.DaoConfig.ConnPoolConfig;
import uw.dao.conf.DaoConfigManager;
import uw.dao.dialect.Dialect;
import uw.dao.dialect.DialectManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据库连接管理器.
 * @author axoen,liliang
 * @since 2018/6/19
 */
public final class ConnectionManager {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    /**
     * 连接池方言
     */
    private static final Map<String, Dialect> SOURCE_DIALECT_MAP = new HashMap<String, Dialect>();

	/**
	 * 数据源缓存表
	 */
	private static final Map<String, HikariDataSource> DATA_SOURCE_MAP = new ConcurrentHashMap<String, HikariDataSource>();

	/**
	 * 启动连接管理器.
	 */
	public static void start() {
        List<String> connList = DaoConfigManager.getConnPoolNameList();
        for (String conn : connList) {
            HikariDataSource dataSource = getDataSource(conn);
            if (dataSource == null) {
                logger.error("Initial ConnectionPool[{}] failed ", conn);
            }
        }
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
	 * 获得一个新连接.
	 * 
	 * @param poolName
	 *            连接池名称
	 * @return 指定的新连接
	 * @throws SQLException
	 *             SQL异常
	 */
	public static Connection getConnection(String poolName) throws SQLException {
        HikariDataSource hikariDataSource = getDataSource(poolName);
        if (hikariDataSource == null) {
            throw new SQLException("ConnectionManager.getConnection() failed to init connPool[" + poolName + "]");
        }
        return hikariDataSource.getConnection();
    }

    /**
     * 获得一个连接的方言，在poolList中排名第一的为默认连接.
     *
     * @return
     * @throws SQLException
     */
    public static Dialect getDialect() throws SQLException {
        return SOURCE_DIALECT_MAP.get("");
    }

    /**
     * 获得一个连接的方言，在poolList中排名第一的为默认连接.
     *
     * @return
     * @throws SQLException
     */
    public static Dialect getDialect(String poolName) throws SQLException {
        return SOURCE_DIALECT_MAP.get(poolName);
    }

	/**
	 * 销毁一个连接池.
	 * 
	 * @param poolName
	 *            连接池名字
	 */
	private static synchronized void destroyConnectionPool(String poolName) {
        HikariDataSource cp = DATA_SOURCE_MAP.get(poolName);
        if (cp != null) {
            DATA_SOURCE_MAP.remove(poolName);
            cp.close();
        }
    }

	/**
	 * 销毁全部连接池.
	 */
	private static synchronized void destroyAllConnectionPool() {
		for (String poolName : DATA_SOURCE_MAP.keySet()) {
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
    private static HikariDataSource getDataSource(String poolName) {
        if (poolName == null) {
            poolName = "";
        }
        return DATA_SOURCE_MAP.computeIfAbsent(poolName, (key) -> {
            ConnPoolConfig config = DaoConfigManager.getConnPoolConfig(key);
            if (config == null) {
                return null;
            }
            int minConns = config.getMinConn();
            if (minConns < 1) {
                minConns = 1;
                config.setMinConn(minConns);
            }
            int maxConns = config.getMaxConn();
            if (maxConns < 1) {
                maxConns = 1;
                config.setMaxConn(maxConns);
            }
            int connIdleTimeout = config.getConnIdleTimeout() * 1000;
            if (connIdleTimeout < 60000) {
                // 最小一分钟
                connIdleTimeout = 60000;
                config.setConnIdleTimeout(60);
            }
            int connBusyTimeout = config.getConnBusyTimeout() * 1000;
            if (connBusyTimeout < 30000) {
                // 最小30秒钟
                connBusyTimeout = 30000;
                config.setConnBusyTimeout(30);
            }
            int connMaxAge = config.getConnMaxAge() * 1000;
            if (connMaxAge < 600000) {
                // 最小60分钟
                connMaxAge = 600000;
                config.setConnMaxAge(60);
            }
            // HikariConfig
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setPoolName(StringUtils.isBlank(key) ? "root" : key);
            // 数据库驱动
            hikariConfig.setDriverClassName(config.getDriver());
            // 服务器连接字符串
            hikariConfig.setJdbcUrl(config.getUrl());
            // 登陆用户名
            hikariConfig.setUsername(config.getUsername());
            // 登陆密码
            hikariConfig.setPassword(config.getPassword());
            // 测试sql hikari不配置testSql,会直接使用Connection.isValid()检活
            if (!hikariConfig.getDriverClassName().contains("mysql")) {
                hikariConfig.setConnectionTestQuery(config.getTestSql());
            }
            // 最小空闲连接数
            hikariConfig.setMinimumIdle(minConns);
            // 最大连接数
            hikariConfig.setMaximumPoolSize(maxConns);
            // 空闲超时(秒钟)
            hikariConfig.setIdleTimeout(connIdleTimeout);
            // 连接超时(秒钟)
            hikariConfig.setConnectionTimeout(connBusyTimeout);
            // 连接寿命(秒钟)
            hikariConfig.setMaxLifetime(connMaxAge);
            // 数据库方言
            HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
            // 注册成功,初始化方言
            SOURCE_DIALECT_MAP.put(key, DialectManager.getDialectByDriverClassName(hikariConfig.getDriverClassName()));
            // 启动连接池
            return hikariDataSource;
        });
    }
}
