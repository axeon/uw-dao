package uw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uw.dao.conf.DMConfigManager;
import uw.dao.connectionpool.ConnectionManager;

/**
 * sequence管理器。 如果不是使用sequence管理器，在集群环境中将有一些麻烦。 系统会自动判定启用那种方式的id生成方式.
 * 
 * @author zhangjin
 */
public class SequenceManager {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(SequenceManager.class);
	/**
	 * insertSQL语句.
	 */
	private static final String INSERT_ID = "insert into sys_sequence (table_name,sequence_id,table_desc,increment_num,create_date,last_update) values(?,?,?,?,?,?)";
	/**
	 * selectSQL语句.
	 */
	private static final String LOAD_ID = "select sequence_id,increment_num from sys_sequence where table_name=? ";
	/**
	 * SequenceManager集合.
	 */
	private static Map<String, SequenceManager> managers = new ConcurrentHashMap<String, SequenceManager>();

	/**
	 * updateSQL语句.
	 */
	private static final String UPDATE_ID = "update sys_sequence set sequence_id=?,last_update=? where table_name=? and sequence_id=?";

	/**
	 * 返回指定的表的sequenceId数值.
	 * 
	 * @param tablename
	 *            表名
	 * @return 下一个值
	 */
	public static long nextId(String tablename) {
		return nextSysSequence(tablename, 1);
	}

	/**
	 * 申请一个Id号码范围.
	 * 
	 * @param tablename
	 *            表名
	 * @param range
	 *            申请多少个号码
	 * @return 起始号码
	 */
	public static long allocateIdRange(String tablename, int range) {
		return nextSysSequence(tablename, range);
	}

	/**
	 * 返回下一个可以取到的id值,功能上类似于数据库的自动递增字段.
	 * 
	 * @param value
	 *            递增累加值
	 */
	private synchronized long nextUniqueID(int value) {
		if (currentId.get() + value > maxId) {
			getNextBlock(value);
		}
		return currentId.addAndGet(value);
	}

	/**
	 * 返回指定的表的sequenceId数值.
	 * 
	 * @param tablename
	 *            表名
	 * @param value
	 *            递增累加值
	 * @return 下一个值
	 */
	private static long nextSysSequence(String tablename, int value) {
		tablename = tablename.toLowerCase();
		SequenceManager manager = managers.get(tablename);
		if (manager == null) {
			manager = initSequenceManager(tablename);
		}
		if (manager != null) {
			return manager.nextUniqueID(value);
		} else {
			return -1;
		}
	}

	/**
	 * 初始化SequenceManager.
	 * 
	 * @param tablename
	 *            表名
	 * @return SequenceManager对象
	 */
	private synchronized static SequenceManager initSequenceManager(String tablename) {
		SequenceManager manager = managers.get(tablename);
		if (manager == null) {
			manager = new SequenceManager(tablename);
			managers.put(tablename, manager);
		}
		return manager;
	}

	/**
	 * 当前id.
	 */
	private AtomicLong currentId = new AtomicLong(0);

	/**
	 * 当前可以获取的最大id.
	 */
	private long maxId;

	/**
	 * 增量数.
	 */
	private int incrementNum;

	/**
	 * table名称.
	 */
	private String sequenceName;

	/**
	 * 建立一个Sequence实例.
	 * 
	 * @param sequenceName
	 *            table名称
	 */
	public SequenceManager(String sequenceName) {
		this.sequenceName = sequenceName;
		this.incrementNum = 1;
	}

	/**
	 * 通过多次尝试获得下一组sequenceId.
	 * 
	 * @param value
	 *            递增累加值
	 */
	private void getNextBlock(int value) {

		for (int i = 0; i < 100; i++) {
			if (getNextBlockImpl(value)) {
				break;
			}
			logger.warn("WARNING: SequenceManager failed to obtain Sequence[{}] next ID block . Trying {}...",
					this.sequenceName, i);
			// 如果不成功，再次调用改方法。
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 执行一个查找下一个sequenceId的操作。步骤如下：
	 * <ol>
	 * <li>从当前的数据库中select出当前的Id
	 * <li>自动递增id。
	 * <li>Update db row with new id where id=old_id.
	 * <li>如果update失败，会重复执行，直至成功。
	 * </ol>
	 * 
	 * @param value
	 *            递增累加值
	 * @return boolean
	 */
	private boolean getNextBlockImpl(int value) {
		boolean success = false;
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = ConnectionManager.getConnection(DMConfigManager.getRouteMapping("sys_sequence", "all"));
			// 从数据库中获取当前值。
			loadSeq();
			// 自动递增id到我们规定的递增累加值。
			long newID = currentId.get() + incrementNum + value;
			// UPDATE_ID语句增加sequenceId的条件判断是让更新更能有效的进行。
			pstmt = con.prepareStatement(UPDATE_ID);
			pstmt.setLong(1, newID);
			pstmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			pstmt.setString(3, sequenceName);
			pstmt.setLong(4, currentId.get());
			// 检查数据库更新是否成功。如果成功，则重新给currentId和maxId赋值。
			success = pstmt.executeUpdate() == 1;
			if (success) {
				this.maxId = newID;
			}
		} catch (Exception sqle) {
			logger.error("GetNextBlock Error!", sqle);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return success;
	}

	/**
	 * 载入序列.
	 */
	private void loadSeq() {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = ConnectionManager.getConnection(DMConfigManager.getRouteMapping("sys_sequence", "all"));
			// 从数据库中获取当前值。
			pstmt = con.prepareStatement(LOAD_ID);
			pstmt.setString(1, sequenceName);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.next()) {
				initSeq();
			} else {
				currentId.set(rs.getLong(1));
				incrementNum = rs.getInt(2);
			}
		} catch (Exception sqle) {
			logger.error("loadSeq Error!", sqle);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

	}

	/**
	 * 初始化序列.
	 */
	private void initSeq() {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = ConnectionManager.getConnection(DMConfigManager.getRouteMapping("sys_sequence", "all"));
			// 从数据库中获取当前值。
			pstmt = con.prepareStatement(INSERT_ID);
			pstmt.setString(1, sequenceName);
			pstmt.setLong(2, currentId.get());
			pstmt.setString(3, sequenceName);
			pstmt.setInt(4, incrementNum);
			pstmt.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
			pstmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis()));
			pstmt.executeUpdate();
		} catch (Exception sqle) {
			logger.error("initSeq exception!", sqle);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

}
