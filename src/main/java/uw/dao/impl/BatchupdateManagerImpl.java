package uw.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uw.dao.BatchupdateManager;
import uw.dao.TransactionException;

/**
 * BatchupdateManager实现类.
 * 
 * @author axeon
 */
public class BatchupdateManagerImpl implements BatchupdateManager {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(BatchupdateManagerImpl.class);

	/**
	 * pstmt集合.
	 */
	private LinkedHashMap<String, PreparedStatement> map = null;

	/**
	 * 是否批量模式.
	 */
	private boolean isBatch = false;

	/**
	 * 批量更新大小.
	 */
	private int batchSize = 50;

	/**
	 * 批量更新的sizeMap.
	 */
	private LinkedHashMap<String, Integer> sizeMap = null;

	/**
	 * 批量更新的resultMap.
	 */
	private LinkedHashMap<String, ArrayList<Integer>> resultMap = null;

	/**
	 * 默认构造器,只能在本包内调用.
	 */
	protected BatchupdateManagerImpl() {
	}

	/**
	 * 获取pstmt.
	 * 
	 * @param conn
	 *            Connection对象
	 * @param sql
	 *            SQL语句
	 * @return PreparedStatement对象
	 * @throws SQLException
	 *             SQL异常
	 */
	public PreparedStatement prepareStatement(Connection conn, String sql) throws SQLException {
		// pstmt对象
		PreparedStatement pstmt = null;

		if (!this.isBatch) { // 非batchupdate
			pstmt = conn.prepareStatement(sql);
		} else { // 进入update状态
			int bsize = 0; // 该pstmt已经addBatch的次数
			if (map.containsKey(sql)) {
				pstmt = map.get(sql);
				// 判断连接是否有效，是否打开事务处理
				if (pstmt.getConnection().isClosed() || pstmt.getConnection().getAutoCommit()) {
					throw new SQLException("TransactionException in BatchUpdateManagerImpl.java:prepareStatement()");
				}
				bsize = sizeMap.get(sql).intValue() + 1;
			} else {
				pstmt = conn.prepareStatement(sql);
				map.put(sql, pstmt);
				bsize = 1;
				// 初始化返回值
				resultMap.put(sql, new ArrayList<Integer>());
			}
			// 把当前bsize更新
			sizeMap.put(sql, new Integer(bsize));
			try {
				checkToBatchUpdate();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
			}
		}

		// 返回该pstmt
		return pstmt;
	}

	/**
	 * 当调用次方法的时候，自动设置开始transaction.
	 * 
	 * @throws TransactionException
	 *             事务异常
	 */
	public void startBatchUpdate() throws TransactionException {
		this.isBatch = true;
		// 初始化map
		map = new LinkedHashMap<String, PreparedStatement>();
		sizeMap = new LinkedHashMap<String, Integer>();
		resultMap = new LinkedHashMap<String, ArrayList<Integer>>();
	}

	/**
	 * 设置批量更新的数量.
	 * 
	 * @param batchSize
	 *            数量
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	/**
	 * 获得批量更新的数量.
	 * 
	 * @return boolean
	 */
	public int getBatchSize() {
		return this.batchSize;
	}

	/**
	 * 获得是否在批量模式下.
	 * 
	 * @throws TransactionException
	 *             事务异常
	 * @return boolean
	 */
	public boolean getBatchStatus() {
		return this.isBatch;
	}

	/**
	 * 检查哪些pstmt需要更新.
	 * 
	 * @throws Exception
	 *             异常
	 */
	private void checkToBatchUpdate() throws Exception {
		PreparedStatement pstmt = null;
		String key = null;
		Iterator<String> it = sizeMap.keySet().iterator();
		while (it.hasNext()) {
			// 获得相关sql
			key = it.next();
			// 检查size是否越界,如果不越界直接进入下一个循环
			if (sizeMap.get(key).intValue() < this.batchSize) {
				continue;
			}
			// 开始做批量更新
			pstmt = map.get(key);
			// 先执行未完成执行的batchupdate
			int[] effects = pstmt.executeBatch();
			// 加入结果map
			List<Integer> list = resultMap.get(key);
			for (int i = 0; i < effects.length; i++) {
				list.add(effects[i]);
			}
			// 更新batch次数，安全起见，先删除原有value
			sizeMap.remove(key);
			sizeMap.put(key, new Integer(0));
		}
	}

	/**
	 * 完成需要清空map，并关闭全部pstmt.
	 * 
	 * @return 结果List
	 * @throws TransactionException
	 *             事务异常
	 */
	public List<List<Integer>> submit() throws TransactionException {
		PreparedStatement pstmt = null;
		Object key = null;
		Iterator<String> it = map.keySet().iterator();
		ArrayList<List<Integer>> resultlist = new ArrayList<List<Integer>>(map.size());
		while (it.hasNext()) {
			// 获得相关sql和pstmt
			key = it.next();
			pstmt = map.get(key);
			try {
				// 先执行未完成执行的batchupdate
				int[] effects = pstmt.executeBatch();
				// 加入结果map
				List<Integer> list = resultMap.get(key);
				for (int i = 0; i < effects.length; i++) {
					list.add(effects[i]);
				}
				resultlist.add(list);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				throw new TransactionException("TransactionException in DbTransactionManager.java:commit()", e);
			} finally {
				try {
					// 最后关闭该pstmt
					pstmt.close();
					// 从map中remove该条
					it.remove();
					map.remove(key);
					// 从sizeMap中remove该条
					sizeMap.remove(key);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		this.isBatch = false;
		map = null;
		sizeMap = null;
		resultMap = null;
		batchSize = 50;
		return resultlist;
	}

	/**
	 * 获得Batch的sql列表.
	 * 
	 * @return sql列表
	 */
	public List<String> getBatchList() {
		ArrayList<String> sqllist = new ArrayList<String>(sizeMap.size());
		Iterator<String> it = sizeMap.keySet().iterator();
		while (it.hasNext()) {
			// 获得相关sql和pstmt
			sqllist.add(it.next());
		}
		return sqllist;
	}

}
