package uw.dao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.DaoFactory;
import uw.dao.TransactionException;
import uw.dao.conf.DaoConfigManager;
import uw.dao.vo.SqlExecuteStats;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 性能计数器,将性能数据输出到mysql中.
 *
 * @author axeon
 */
public class StatsLogService {
    /**
     * 存储的表名.
     */
    public static final String STATS_BASE_TABLE = "dao_sql_stats";
    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(StatsLogService.class);
    /**
     * DAOFactory对象.
     */
    private static final DaoFactory dao = DaoFactory.getInstance();
    /**
     * 是否已经启动.
     */
    private static final AtomicBoolean isStarted = new AtomicBoolean(false);

    /**
     * 数据集合.
     */
    private static ArrayList<SqlExecuteStats> datalist = new ArrayList<>();

    /**
     * pageLog的写入锁.
     */
    private static final ReentrantLock locker = new ReentrantLock();

    /**
     * 可以记录的sqlCostMin最小时间。
     */
    private static int sqlCostMin = 30;

    /**
     * 开始任务.
     */
    public static void start() {
        isStarted.compareAndSet(false, true);
        checkForCreatesStatsTable();
        try {
            sqlCostMin = DaoConfigManager.getConfig().getSqlStats().getSqlCostMin();
        } catch (Throwable e) {
        }
    }

    /**
     * 停止任务.
     */
    public static void stop() {
        isStarted.compareAndSet(true, false);
    }

    /**
     * 是否已启动.
     *
     * @return boolean
     */
    public static boolean isStarted() {
        return isStarted.get();
    }

    /**
     * 记录性能参数.
     *
     * @param ses 用于统计sql执行的性能数据
     */
    public static void logStats(SqlExecuteStats ses) {
        if (ses.getAllTime() >= sqlCostMin) {
            if (isStarted.get()) {
                locker.lock();
                try {
                    datalist.add(ses);
                } finally {
                    locker.unlock();
                }
            }
        }
    }

    /**
     * 记录性能参数.
     *
     * @param connName  连接名
     * @param sql       执行的具体sql
     * @param param     附加的参数
     * @param rowNum    返回/影响的行数
     * @param dbTime    数据库层操作数据库消耗的时间
     * @param allTime   数据库层消耗的时间
     * @param exception 异常类
     */
    public static void logStats(String connName, int connId, String sql, String param, int rowNum, long connTime, long dbTime, long allTime,
                                String exception) {
        if (allTime >= sqlCostMin) {
            if (isStarted.get()) {
                locker.lock();
                try {
                    datalist.add(new SqlExecuteStats(connName, connId, sql, param, rowNum, connTime, dbTime, allTime, exception));
                } finally {
                    locker.unlock();
                }
            }
        }
    }

    /**
     * 获得sql执行列表，并重新构造列表.
     *
     * @return 列表
     */
    static ArrayList<SqlExecuteStats> getStatsList() {
        ArrayList<SqlExecuteStats> list = null;
        locker.lock();
        try {
            list = datalist;
            datalist = new ArrayList<>();
        } finally {
            locker.unlock();
        }
        return list;
    }

    /**
     * 检查是否应该新建表.
     */
    private static void checkForCreatesStatsTable() {
        String sql = "create table if not exists " + STATS_BASE_TABLE + " (\n"
                + "id bigint(20) NOT NULL AUTO_INCREMENT,\n" + "conn_name varchar(100) DEFAULT NULL,\n" + "conn_id int(11) DEFAULT NULL,\n"
                + "sql_info varchar(1000) DEFAULT NULL,\n" + "sql_param varchar(1000) DEFAULT NULL,\n"
                + "row_num int(11) DEFAULT NULL,\n" + "conn_time int(11) DEFAULT NULL,\n" + "db_time int(11) DEFAULT NULL,\n"
                + "all_time int(11) DEFAULT NULL,\n" + "exception varchar(500) DEFAULT NULL,\n"
                + "exe_date datetime DEFAULT NULL,\n" + "PRIMARY KEY (id)\n" + ") ENGINE=INNODB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=COMPRESSED";
        try {
            dao.executeCommand(dao.getConnectionName(STATS_BASE_TABLE, "all"), sql);
            logger.info("init table: {}", STATS_BASE_TABLE);
        } catch (TransactionException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
