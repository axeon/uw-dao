package uw.dao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import uw.dao.DaoFactory;
import uw.dao.DataSet;
import uw.dao.TransactionException;
import uw.dao.conf.DaoConfig;
import uw.dao.conf.DaoConfig.TableShardConfig;
import uw.dao.conf.DaoConfigManager;
import uw.dao.util.TableShardingUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 按日期分表的工具.
 *
 * @author axeon
 */
public class TableShardingTask implements Runnable {

    /**
     * 日志.
     */
    private static final Logger log = LoggerFactory.getLogger(TableShardingTask.class);
    /**
     * DAOFactory对象.
     */
    private final DaoFactory dao = DaoFactory.getInstance();

    /**
     * dao配置文件。
     */
    private static final DaoConfig daoConfig = DaoConfigManager.getConfig();

    /**
     * 链接内表列表.
     */
    private final HashMap<String, List<String>> tableListMap = new HashMap<>();

    /**
     * 自动建表工具。 每小时检查当天表和第二天的表。 设定一个3秒的延时，是担心同步问题.
     */
    @Override
    public void run() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, TableShardConfig> map = daoConfig.getTableShard();
        for (Map.Entry<String, TableShardConfig> kv : map.entrySet()) {
            String tableName = kv.getKey();
            TableShardConfig tc = kv.getValue();
            if (tc.isAutoGen()) {
                if ("date".equalsIgnoreCase(tc.getShardType())) {
                    // 计算当前表和下一个表
                    String current = "", next = "";
                    switch (tc.getShardRule()) {
                        case "day":
                            current = now.format(TableShardingUtils.FORMATTER_DAY);
                            next = now.plusDays(1).format(TableShardingUtils.FORMATTER_DAY);
                            break;
                        case "month":
                            current = now.format(TableShardingUtils.FORMATTER_MONTH);
                            next = now.plusMonths(1).format(TableShardingUtils.FORMATTER_MONTH);
                            break;
                        case "year":
                            current = now.format(TableShardingUtils.FORMATTER_YEAR);
                            next = now.plusYears(1).format(TableShardingUtils.FORMATTER_YEAR);
                            break;
                        default:
                            current = now.format(TableShardingUtils.FORMATTER_DAY);
                            next = now.plusDays(1).format(TableShardingUtils.FORMATTER_DAY);
                            break;
                    }
                    current = tableName + "_" + current;
                    next = tableName + "_" + next;
                    String createScript = null;
                    if (!checkTableExist(current)) {
                        if (createScript == null) {
                            createScript = getCreateScript(tableName);
                        }
                        exeCreateTable(current, createScript.replaceAll(tableName, current));
                    }
                    if (!checkTableExist(next)) {
                        if (createScript == null) {
                            createScript = getCreateScript(tableName);
                        }
                        exeCreateTable(next, createScript.replaceAll(tableName, next));
                    }
                }
            }
        }
    }

    /**
     * 检查表是否存在.
     *
     * @param tableName 表名
     * @return boolean
     */
    private boolean checkTableExist(String tableName) {
        String connName = dao.getConnectionName(tableName, "all");
        List<String> tablist = tableListMap.get(connName);
        if (tablist == null) {
            tablist = loadTableList(connName);
        }
        return tablist.contains(tableName);
    }

    /**
     * 载入当前连接列表.
     *
     * @param connName 连接名
     * @return 连接列列表
     */
    private List<String> loadTableList(String connName) {
        Connection conn = null;
        ResultSet rs = null;
        List<String> list = new ArrayList<String>();
        try {
            conn = dao.getConnection(connName);
            DatabaseMetaData metaData = conn.getMetaData();
            rs = metaData.getTables(null, null, null, new String[]{"TABLE"});
            while (rs.next()) {
                list.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return list;
    }

    /**
     * 获得建表sql.
     *
     * @param tableName 表名
     * @return 建表sql
     */
    private String getCreateScript(String tableName) {
        String script = null;
        try {
            DataSet ds = dao.queryForDataSet(dao.getConnectionName(tableName, "all"), "show create table " + tableName);
            if (ds.next()) {
                script = ds.getString(2);
            }
        } catch (TransactionException e) {
            log.error(e.getMessage(), e);
        }
        return script;
    }

    /**
     * 建表.
     *
     * @param tableName    表名
     * @param createScript 创建脚本
     * @return int
     */
    private int exeCreateTable(String tableName, String createScript) {
        int effect = 0;
        try {
            effect = dao.executeCommand(dao.getConnectionName(tableName, "all"), createScript);
        } catch (TransactionException e) {
            log.error(e.getMessage(), e);
        }
        return effect;
    }

}
