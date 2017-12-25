package uw.dao.conf;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import uw.dao.conf.DAOConfig.ConnPoolConfig;
import uw.dao.conf.DAOConfig.TableShardingConfig;
import uw.dao.connectionpool.ConnectionManager;
import uw.dao.service.StatsCleanDataTask;
import uw.dao.service.StatsLogService;
import uw.dao.service.StatsLogWriteTask;
import uw.dao.service.TableShardingTask;

/**
 * spring启动配置文件.
 * 
 * @author axeon
 */
@Configuration
@Import({ StatsCleanDataTask.class, StatsLogWriteTask.class, TableShardingTask.class })
@EnableConfigurationProperties({ DAOConfig.class })
public class DAOSpringAutoConfiguration {

	/**
	 * 日志.
	 */
	private static final Logger log = LoggerFactory.getLogger(DAOSpringAutoConfiguration.class);

	/**
	 * DAO配置表.
	 */
	@Autowired
	private DAOConfig daoConfig;

	/**
	 * 配置初始化.
	 */
	@PostConstruct
	public void init() {

		log.info("uw.dao start auto configuration...");

		if (daoConfig != null && daoConfig.getConnPool() != null) {
			ConnPoolConfig rootPoolConfig = daoConfig.getConnPool().getRoot();
			if (rootPoolConfig != null) {
				if (rootPoolConfig.getDriver().contains("mysql")) {
					rootPoolConfig.setDbType("mysql");
				} else if (rootPoolConfig.getDriver().contains("oracle")) {
					rootPoolConfig.setDbType("oracle");
				}
				Map<String, ConnPoolConfig> poolMap = daoConfig.getConnPool().getList();
				if (poolMap != null) {
					// 检查并填充DAOConfig默认值
					for (Entry<String, ConnPoolConfig> kv : poolMap.entrySet()) {
						ConnPoolConfig poolConfig = kv.getValue();
						if (poolConfig.getDriver() == null) {
							poolConfig.setDriver(rootPoolConfig.getDriver());
						}
						if (poolConfig.getDriver().contains("mysql")) {
							poolConfig.setDbType("mysql");
						} else if (poolConfig.getDriver().contains("oracle")) {
							poolConfig.setDbType("oracle");
						}
						if (poolConfig.getTestSql() == null) {
							poolConfig.setTestSql(rootPoolConfig.getTestSql());
						}
						if (poolConfig.getMinConn() == 0) {
							poolConfig.setMinConn(rootPoolConfig.getMinConn());
						}
						if (poolConfig.getMaxConn() == 0) {
							poolConfig.setMaxConn(rootPoolConfig.getMaxConn());
						}
						if (poolConfig.getConnIdleTimeout() == 0) {
							poolConfig.setConnIdleTimeout(rootPoolConfig.getConnIdleTimeout());
						}
						if (poolConfig.getConnBusyTimeout() == 0) {
							poolConfig.setConnBusyTimeout(rootPoolConfig.getConnBusyTimeout());
						}
						if (poolConfig.getConnMaxAge() == 0) {
							poolConfig.setConnMaxAge(rootPoolConfig.getConnMaxAge());
						}
					}
				}
				// 给值
				DAOConfigManager.setConfig(daoConfig);
				// 启动连接池。
				ConnectionManager.start();
			}
		}
		if (daoConfig != null && daoConfig.getSqlStats() != null) {
			if (daoConfig.getSqlStats().isEnable()) {
				// 加入统计日志表到sharding配置中。
				TableShardingConfig config = new TableShardingConfig();
				config.setShardingType("date");
				config.setShardingRule("day");
				config.setAutoGen(true);
				daoConfig.getTableSharding().put(StatsLogService.STATS_BASE_TABLE, config);
				StatsLogService.start();
			}
		}

	}

	/**
	 * 关闭连接管理器,销毁全部连接池.
	 */
	@PreDestroy
	public void destroy() {
		log.info("uw.dao destroy configuration...");
		ConnectionManager.stop();
	}

}
