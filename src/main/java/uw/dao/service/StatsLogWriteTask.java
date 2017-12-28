package uw.dao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uw.dao.DaoFactory;
import uw.dao.SequenceManager;
import uw.dao.util.DaoValueUtils;
import uw.dao.util.TableShardingUtils;
import uw.dao.vo.SqlExecuteStats;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 性能日志写入任务.
 * 
 * @author axeon
 */
@EnableScheduling
public class StatsLogWriteTask {
	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(StatsLogWriteTask.class);
	/**
	 * DAOFactory对象.
	 */
	private DaoFactory dao = DaoFactory.getInstance();

	/**
	 * 3秒写一次数据.
	 */
	@Scheduled(initialDelay = 5000, fixedRate = 10000)
	public void writeData() {
		ArrayList<SqlExecuteStats> list = StatsLogService.getStatsList();
		if (list.size() == 0) {
			return;
		}
		writeStatsList(list);
	}

	/**
	 * 执行数据库插入.
	 * 
	 * @param list
	 *            SqlExecuteStats集合
	 */
	private void writeStatsList(List<SqlExecuteStats> list) {
		String tableName = TableShardingUtils.getTableNameByDate(StatsLogService.STATS_BASE_TABLE,
				list.get(0).getActionDate());
		Connection conn = null;
		PreparedStatement pstmt = null;
		String pdsql = "INSERT INTO " + tableName
				+ "(id,conn_name,sql_info,sql_param,row_num,db_time,all_time,exception,exe_date) values "
				+ "(?,?,?,?,?,?,?,?,?) ";
		int pos = 0;
		try {
			conn = dao.getConnection(tableName, "write");
			conn.setAutoCommit(false);
			pstmt = conn.prepareStatement(pdsql);
			for (pos = 0; pos < list.size(); pos++) {
				SqlExecuteStats ss = list.get(pos);
				// 发现已经跨时间分片了，此时要退出
				if (pos > 0 && !tableName.equals(
						TableShardingUtils.getTableNameByDate(StatsLogService.STATS_BASE_TABLE, ss.getActionDate()))) {
					break;
				}
				pstmt.setLong(1, SequenceManager.nextId(StatsLogService.STATS_BASE_TABLE));
				if (ss.getConnName() != null && ss.getConnName().length() > 100) {
					ss.setConnName(ss.getConnName().substring(0, 100));
				}
				pstmt.setString(2, ss.getConnName());
				if (ss.getSql() != null && ss.getSql().length() > 1000) {
					ss.setSql(ss.getSql().substring(0, 100));
				}
				pstmt.setString(3, ss.getSql());
				if (ss.getParam() != null && ss.getParam().length() > 1000) {
					ss.setParam(ss.getParam().substring(0, 100));
				}
				pstmt.setString(4, ss.getParam());
				pstmt.setInt(5, ss.getRowNum());
				pstmt.setInt(6, (int) ss.getDbTime());
				pstmt.setInt(7, (int) ss.getAllTime());
				if (ss.getException() != null && ss.getException().length() > 500) {
					ss.setException(ss.getException().substring(0, 100));
				}
				pstmt.setString(8, ss.getException());
				pstmt.setTimestamp(9, DaoValueUtils.dateToTimestamp(ss.getActionDate()));
				pstmt.addBatch();
				if ((pos + 1) % 100 == 0 && pos > 0) {
					// 每隔100次自动提交
					pstmt.executeBatch();
				}
			}
			// 剩余部分也要执行提交。
			if (pos % 100 > 0) {
				pstmt.executeBatch();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
				try {
					conn.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		// 接着往这里写
		if (list.size() - pos >= 1) {
			writeStatsList(list.subList(pos, list.size()));
		}
	}

}
