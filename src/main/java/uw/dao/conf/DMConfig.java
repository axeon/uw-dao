package uw.dao.conf;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * DM配置表.
 * 
 * @author axeon
 */
@ConfigurationProperties(prefix = "uw.dm")
public class DMConfig {

	/**
	 * 连接池配置.
	 */
	private ConnPool connPool = new ConnPool();

	/**
	 * 连接路由配置.
	 */
	private ConnRoute connRoute = new ConnRoute();

	/**
	 * 表分片配置.
	 */
	private Map<String, TableShardingConfig> tableSharding = new HashMap<String, TableShardingConfig>();

	/**
	 * sql统计配置.
	 */
	private SqlStatsConfig sqlStats = new SqlStatsConfig();

	/**
	 * @return the connPool
	 */
	public ConnPool getConnPool() {
		return connPool;
	}

	/**
	 * @param connPool
	 *            the connPool to set
	 */
	public void setConnPool(ConnPool connPool) {
		this.connPool = connPool;
	}

	/**
	 * @return the connRoute
	 */
	public ConnRoute getConnRoute() {
		return connRoute;
	}

	/**
	 * @param connRoute
	 *            the connRoute to set
	 */
	public void setConnRoute(ConnRoute connRoute) {
		this.connRoute = connRoute;
	}

	/**
	 * @return the tableSharding
	 */
	public Map<String, TableShardingConfig> getTableSharding() {
		return tableSharding;
	}

	/**
	 * @param tableSharding
	 *            the tableSharding to set
	 */
	public void setTableSharding(Map<String, TableShardingConfig> tableSharding) {
		this.tableSharding = tableSharding;
	}

	/**
	 * @return the sqlStats
	 */
	public SqlStatsConfig getSqlStats() {
		return sqlStats;
	}

	/**
	 * @param sqlStats
	 *            the sqlStats to set
	 */
	public void setSqlStats(SqlStatsConfig sqlStats) {
		this.sqlStats = sqlStats;
	}

	/**
	 * 连接池配置.
	 */
	public static class ConnPool {
		/**
		 * 默认的连接池.
		 */
		private ConnPoolConfig root;

		/**
		 * 连接路由表.
		 */
		private Map<String, ConnPoolConfig> list;

		/**
		 * @return the root
		 */
		public ConnPoolConfig getRoot() {
			return root;
		}

		/**
		 * @param root
		 *            the root to set
		 */
		public void setRoot(ConnPoolConfig root) {
			this.root = root;
		}

		/**
		 * @return the list
		 */
		public Map<String, ConnPoolConfig> getList() {
			return list;
		}

		/**
		 * @param list
		 *            the list to set
		 */
		public void setList(Map<String, ConnPoolConfig> list) {
			this.list = list;
		}

	}

	/**
	 * 连接路由配置.
	 */
	public static class ConnRoute {
		/**
		 * 默认的链接路由.
		 */
		private ConnRouteConfig root;

		/**
		 * 连接路由表.
		 */
		private Map<String, ConnRouteConfig> list;

		/**
		 * @return the root
		 */
		public ConnRouteConfig getRoot() {
			return root;
		}

		/**
		 * @param root
		 *            the root to set
		 */
		public void setRoot(ConnRouteConfig root) {
			this.root = root;
		}

		/**
		 * @return the list
		 */
		public Map<String, ConnRouteConfig> getList() {
			return list;
		}

		/**
		 * @param list
		 *            the list to set
		 */
		public void setList(Map<String, ConnRouteConfig> list) {
			this.list = list;
		}

	}

	/**
	 * 连接池配置.
	 *
	 * @author axeon
	 */
	public static class ConnPoolConfig {

		/**
		 * 数据库类型.
		 */
		private String dbType;

		/**
		 * 数据库驱动.
		 */
		private String driver;

		/**
		 * 服务器连接地址.
		 */
		private String url;

		/**
		 * 登录用户名.
		 */
		private String username;

		/**
		 * 登录密码.
		 */
		private String password;

		/**
		 * 测试sql，用于测试连接是否可用.
		 */
		private String testSql;

		/**
		 * 最小连接数.
		 */
		private int minConn;

		/**
		 * 最大连接数.
		 */
		private int maxConn;

		/**
		 * 连接闲时超时秒数.
		 */
		private int connIdleTimeout;

		/**
		 * 连接忙时超时秒数.
		 */
		private int connBusyTimeout;

		/**
		 * 连接最大寿命秒数.
		 */
		private int connMaxAge;

		/**
		 * @return the dbType
		 */
		public String getDbType() {
			return dbType;
		}

		/**
		 * @param dbType
		 *            the dbType to set
		 */
		public void setDbType(String dbType) {
			this.dbType = dbType;
		}

		/**
		 * @return the driver
		 */
		public String getDriver() {
			return driver;
		}

		/**
		 * @param driver
		 *            the driver to set
		 */
		public void setDriver(String driver) {
			this.driver = driver;
		}

		/**
		 * @return the url
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * @param url
		 *            the url to set
		 */
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * @return the username
		 */
		public String getUsername() {
			return username;
		}

		/**
		 * @param username
		 *            the username to set
		 */
		public void setUsername(String username) {
			this.username = username;
		}

		/**
		 * @return the password
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @param password
		 *            the password to set
		 */
		public void setPassword(String password) {
			this.password = password;
		}

		/**
		 * @return the testSql
		 */
		public String getTestSql() {
			return testSql;
		}

		/**
		 * @param testSql
		 *            the testSql to set
		 */
		public void setTestSql(String testSql) {
			this.testSql = testSql;
		}

		/**
		 * @return the minConn
		 */
		public int getMinConn() {
			return minConn;
		}

		/**
		 * @param minConn
		 *            the minConn to set
		 */
		public void setMinConn(int minConn) {
			this.minConn = minConn;
		}

		/**
		 * @return the maxConn
		 */
		public int getMaxConn() {
			return maxConn;
		}

		/**
		 * @param maxConn
		 *            the maxConn to set
		 */
		public void setMaxConn(int maxConn) {
			this.maxConn = maxConn;
		}

		/**
		 * @return the connIdleTimeout
		 */
		public int getConnIdleTimeout() {
			return connIdleTimeout;
		}

		/**
		 * @param connIdleTimeout
		 *            the connIdleTimeout to set
		 */
		public void setConnIdleTimeout(int connIdleTimeout) {
			this.connIdleTimeout = connIdleTimeout;
		}

		/**
		 * @return the connBusyTimeout
		 */
		public int getConnBusyTimeout() {
			return connBusyTimeout;
		}

		/**
		 * @param connBusyTimeout
		 *            the connBusyTimeout to set
		 */
		public void setConnBusyTimeout(int connBusyTimeout) {
			this.connBusyTimeout = connBusyTimeout;
		}

		/**
		 * @return the connMaxAge
		 */
		public int getConnMaxAge() {
			return connMaxAge;
		}

		/**
		 * @param connMaxAge
		 *            the connMaxAge to set
		 */
		public void setConnMaxAge(int connMaxAge) {
			this.connMaxAge = connMaxAge;
		}

	}

	/**
	 * 链接路由配置.
	 *
	 * @author axeon
	 */
	public static class ConnRouteConfig {

		/**
		 * 全权限连接.
		 */
		private String all;

		/**
		 * 写连接.
		 */
		private String write;

		/**
		 * 读连接.
		 */
		private String read;

		/**
		 * @return the all
		 */
		public String getAll() {
			return all;
		}

		/**
		 * @param all
		 *            the all to set
		 */
		public void setAll(String all) {
			this.all = all;
		}

		/**
		 * @return the write
		 */
		public String getWrite() {
			return write;
		}

		/**
		 * @param write
		 *            the write to set
		 */
		public void setWrite(String write) {
			this.write = write;
		}

		/**
		 * @return the read
		 */
		public String getRead() {
			return read;
		}

		/**
		 * @param read
		 *            the read to set
		 */
		public void setRead(String read) {
			this.read = read;
		}

	}

	/**
	 * 多表配置.
	 *
	 * @author axeon
	 */
	public static class TableShardingConfig {
		/**
		 * 分片类型。 当前仅支持date类型.
		 */
		private String shardingType;

		/**
		 * 分片规则。当前仅支持day,month,year类型.
		 */
		private String shardingRule;

		/**
		 * 是否自动建表.
		 */
		private boolean autoGen;

		/**
		 * @return the shardingType
		 */
		public String getShardingType() {
			return shardingType;
		}

		/**
		 * @param shardingType
		 *            the shardingType to set
		 */
		public void setShardingType(String shardingType) {
			this.shardingType = shardingType;
		}

		/**
		 * @return the shardingRule
		 */
		public String getShardingRule() {
			return shardingRule;
		}

		/**
		 * @param shardingRule
		 *            the shardingRule to set
		 */
		public void setShardingRule(String shardingRule) {
			this.shardingRule = shardingRule;
		}

		/**
		 * @return the autoGen
		 */
		public boolean isAutoGen() {
			return autoGen;
		}

		/**
		 * @param autoGen
		 *            the autoGen to set
		 */
		public void setAutoGen(boolean autoGen) {
			this.autoGen = autoGen;
		}

	}

	/**
	 * 统计sql执行信息，包括参数，返回信息，执行时间等,表名为dm_sql_stats开头，此表被自动配置为按日分表。.
	 *
	 * @author axeon
	 */
	public static class SqlStatsConfig {

		/**
		 * 是否统计，默认是false.
		 */
		private boolean enable = false;

		/**
		 * 保存时间，默认是100天.
		 */
		private int dataKeepDays = 100;

		/**
		 * @return the enable
		 */
		public boolean isEnable() {
			return enable;
		}

		/**
		 * @param enable
		 *            the enable to set
		 */
		public void setEnable(boolean enable) {
			this.enable = enable;
		}

		/**
		 * @return the dataKeepDays
		 */
		public int getDataKeepDays() {
			return dataKeepDays;
		}

		/**
		 * @param dataKeepDays
		 *            the dataKeepDays to set
		 */
		public void setDataKeepDays(int dataKeepDays) {
			this.dataKeepDays = dataKeepDays;
		}

	}

}
