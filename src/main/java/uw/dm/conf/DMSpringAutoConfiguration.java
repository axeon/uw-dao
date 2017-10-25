package uw.dm.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uw.dm.conf.DMConfig.ConnPoolConfig;
import uw.dm.conf.DMConfig.TableShardingConfig;
import uw.dm.connectionpool.ConnectionManager;
import uw.dm.service.StatsCleanDataTask;
import uw.dm.service.StatsLogService;
import uw.dm.service.StatsLogWriteTask;
import uw.dm.service.TableShardingTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Map.Entry;

/**
 * spring启动配置文件.
 * @author axeon
 */
@Configuration
@Import({StatsCleanDataTask.class, StatsLogWriteTask.class, TableShardingTask.class})
@EnableConfigurationProperties({DMConfig.class})
public class DMSpringAutoConfiguration {

    /**
     * 日志.
     */
    private static final Logger log = LoggerFactory.getLogger(DMSpringAutoConfiguration.class);

    /**
     * DM配置表.
     */
    @Autowired
    private DMConfig dmConfig;

    /**
     * 配置初始化.
     */
    @PostConstruct
    public void init() {

        log.info("uw.dm start auto configuration...");

        if (dmConfig != null && dmConfig.getConnPool() != null) {
            ConnPoolConfig rootPoolConfig = dmConfig.getConnPool().getRoot();
            if (rootPoolConfig != null) {
                if (rootPoolConfig.getDriver().contains("mysql")) {
                    rootPoolConfig.setDbType("mysql");
                } else if (rootPoolConfig.getDriver().contains("oracle")) {
                    rootPoolConfig.setDbType("oracle");
                }
                Map<String, ConnPoolConfig> poolMap = dmConfig.getConnPool().getList();
                if (poolMap != null) {
                    // 检查并填充DMConfig默认值
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
                DMConfigManager.setConfig(dmConfig);
                // 启动连接池。
                ConnectionManager.start();
            }
        }
        if (dmConfig != null && dmConfig.getSqlStats() != null) {
            if (dmConfig.getSqlStats().isEnable()) {
                // 加入统计日志表到sharding配置中。
                TableShardingConfig config = new TableShardingConfig();
                config.setShardingType("date");
                config.setShardingRule("day");
                config.setAutoGen(true);
                dmConfig.getTableSharding().put(StatsLogService.STATS_BASE_TABLE, config);
                StatsLogService.start();
            }
        }

    }

    /**
     * 关闭连接管理器,销毁全部连接池.
     */
    @PreDestroy
    public void destroy() {
        log.info("uw.dm destroy configuration...");
        ConnectionManager.stop();
    }

}
