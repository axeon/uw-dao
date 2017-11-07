package uw.dm.impl;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uw.dm.DataEntity;
import uw.dm.DataList;
import uw.dm.TransactionException;
import uw.dm.annotation.ColumnMeta;
import uw.dm.annotation.TableMeta;
import uw.dm.conf.DMConfigManager;
import uw.dm.dialect.Dialect;
import uw.dm.dialect.DialectManager;
import uw.dm.util.DmReflectUtils;
import uw.dm.vo.FieldMetaInfo;
import uw.dm.vo.TableMetaInfo;

/**
 * 实体类命令实现.
 * @author axeon
 */
public class EntityCommandImpl {

	/**
	 * 日志.
	 */
	private static final Logger logger = LoggerFactory.getLogger(EntityCommandImpl.class);

	/**
	 * 实体信息缓存.
	 */
	private static HashMap<String, TableMetaInfo> entityMetaCache = new HashMap<String, TableMetaInfo>();

    /**
     * 实体类支持的最大继承层级
     */
	private static final int MAX_ENTITY_CLASS_EXTEND_LEVEL = 10;
	/**
	 * 保存一个实体.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名字
	 * @param entity 实体类
	 * @param tableName 表名
	 * @param <T> 实体类类型
	 * @return 实体类
	 * @throws TransactionException 事务异常
	 */
	@SuppressWarnings("resource")
	public static <T extends DataEntity> T save(DAOFactoryImpl dao, String connName, T entity, String tableName) throws TransactionException {
			long start = System.currentTimeMillis();
		long dbTime = -1;
		String exception = null;
		TableMetaInfo emi = loadEntityMetaInfo(entity.getClass());
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + entity.getClass() + "] not found! ");
		}
		if (tableName == null || tableName.equals("")) {
			tableName = emi.getTableName();
		}

		if (connName == null || connName.equals("")) {
			connName = DMConfigManager.getRouteMapping(tableName, "write");
		}
		StringBuilder sb = new StringBuilder();
		// 写入所有的列
		ArrayList<String> cols = new ArrayList<String>(emi.getColumnMap().keySet());
		if (cols.size() > 0) {
			sb.append("insert into ").append(tableName).append(" (");
			for (String col : cols) {
				sb.append(col).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(") values (");
			for (int i = 0; i < cols.size(); i++) {
				sb.append("?,");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")");
		}
		List<FieldMetaInfo> pks = emi.getPklist();
		String[] pkcols = new String[pks.size()];
		for (int i = 0; i < pks.size(); i++) {
			pkcols[i] = pks.get(i).getColumnName();
		}
		Connection con = null;
		PreparedStatement pstmt = null;
		int effect = 0;
		try {
			con = dao.getTransactionController().getConnection(connName);
			pstmt = con.prepareStatement(sb.toString(), pkcols);
			int seq = 0;
			for (String col : cols) {
				FieldMetaInfo fmi = emi.getFieldMetaInfo(col);
				if (fmi == null) {
					throw new TransactionException("FieldMetaInfo[" + col + "@" + entity.getClass() + "] not found! ");
				}
				DmReflectUtils.DAOLiteSaveReflect(pstmt, entity, fmi, ++seq);
			}
			long dbStart = System.currentTimeMillis();
			effect = pstmt.executeUpdate();
			dbTime = System.currentTimeMillis() - dbStart;
		} catch (Exception e) {
			exception = e.toString();
			throw new TransactionException("TransactionException in DAOCommandImpl.java:save()", e);
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dao.getTransactionController().isAutoCommit() && con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, sb.toString(), entity.GET_UPDATED_INFO(), effect, dbTime, allTime, exception);
		return entity;
	}
	/**
	 * 加载一个实体.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名
	 * @param cls 要映射的对象类型
	 * @param tableName 表名
	 * @param id 主键
	 * @param <T> 要映射的对象类型
	 * @return 实体类
	 * @throws TransactionException 事务异常
	 */
	public static <T> T load(DAOFactoryImpl dao, String connName, Class<T> cls, String tableName, Serializable id) throws TransactionException {
		long start = System.currentTimeMillis();
		long dbTime = -1;
		int rowNum = 0;
		String exception = null;
		TableMetaInfo emi = loadEntityMetaInfo(cls);
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + cls + "] not found! ");
		}
		if (tableName == null || tableName.equals("")) {
			tableName = emi.getTableName();
		}

		if (connName == null || connName.equals("")) {
			connName = DMConfigManager.getRouteMapping(tableName, "write");
		}
		StringBuilder sb = new StringBuilder();
		List<FieldMetaInfo> pks = emi.getPklist();
		sb.append("select * from ").append(tableName).append(" where ");
		if (pks.size() > 0) {
			FieldMetaInfo fmi = pks.get(0);
			sb.append(fmi.getColumnName()).append("=? ");
		}

		T entity = null;

		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = dao.getTransactionController().getConnection(connName);
			pstmt = con.prepareStatement(sb.toString());
			int i = 0;
			DmReflectUtils.CommandUpdateReflect(pstmt, i + 1, id);
			long dbStart = System.currentTimeMillis();
			ResultSet rs = pstmt.executeQuery();
			dbTime = System.currentTimeMillis() - dbStart;

			// 获得字段列表
			ResultSetMetaData rsm = rs.getMetaData();
			int colsCount = rsm.getColumnCount();
			String[] cols = new String[colsCount];
			for (int k = 0; k < colsCount; k++) {
				cols[k] = rsm.getColumnLabel(k + 1).toLowerCase();
			}

			if (rs.next()) {
				rowNum = 1;
				entity = cls.newInstance();
				for (String col : cols) {
					FieldMetaInfo fmi = emi.getFieldMetaInfo(col);
					if (fmi != null) {
						DmReflectUtils.DAOLiteLoadReflect(rs, entity, fmi);
					}
				}
			}
		} catch (Exception e) {
			exception = e.toString();
			throw new TransactionException("TransactionException in EntityCommandImpl.load()", e);
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
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, sb.toString(), id.toString(), rowNum, dbTime, allTime, exception);
		return entity;
	}

	/**
	 * 加载一个实体.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名
	 * @param cls 要映射的对象类型
	 * @param selectsql 查询的SQL语句
	 * @param paramList 参数的Object数组
	 * @param <T> 要映射的对象类型
	 * @return 实体类
	 * @throws TransactionException 事务异常
	 */
	public static <T> T listSingle(DAOFactoryImpl dao, String connName, Class<T> cls, String selectsql, Object[] paramList) throws TransactionException {
		long start = System.currentTimeMillis();
		long dbTime = -1;
		int rowNum = 0;
		String exception = null;
		if (connName == null) {
			connName = SQLUtils.getConnNameFromSQL(selectsql);
		}
		Connection con = null;
		PreparedStatement pstmt = null;
		TableMetaInfo emi = loadEntityMetaInfo(cls);
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + cls.getName() + "] not found! ");
		}
		T entity = null;

		try {
			con = dao.getTransactionController().getConnection(connName);
			pstmt = con.prepareStatement(selectsql);
			int i = 0;

			if (paramList != null && paramList.length > 0) {
				for (i = 0; i < paramList.length; i++) {
					DmReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
				}
			}
			long dbStart = System.currentTimeMillis();
			ResultSet rs = pstmt.executeQuery();
			dbTime = System.currentTimeMillis() - dbStart;

			// 获得字段列表
			ResultSetMetaData rsm = rs.getMetaData();
			int colsCount = rsm.getColumnCount();
			String[] cols = new String[colsCount];
			for (int k = 0; k < colsCount; k++) {
				cols[k] = rsm.getColumnLabel(k + 1).toLowerCase();
			}

			if (rs.next()) {
				rowNum = 1;
				entity = cls.newInstance();
				for (String col : cols) {
					FieldMetaInfo fmi = emi.getFieldMetaInfo(col);
					if (fmi != null) {
						DmReflectUtils.DAOLiteLoadReflect(rs, entity, fmi);
					}
				}
			}
		} catch (Exception e) {
			exception = e.toString();
			throw new TransactionException("TransactionException in EntityCommandImpl.listSingle()", e);
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
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, selectsql, Arrays.toString(paramList), rowNum, dbTime, allTime, exception);
		return entity;
	}
	/**
	 * 保存一个实体.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名
	 * @param entity 实体类
	 * @param tableName 表名
	 * @return 实体类
	 * @throws TransactionException 事务异常
	 */
	public static int update(DAOFactoryImpl dao, String connName, DataEntity entity, String tableName) throws TransactionException {
		// 有时候从数据库中load数据，并无实质更新，此时直接返回-1.
		if (entity.GET_UPDATED_COLUMN() == null) {
			return -1;
		}
		long start = System.currentTimeMillis();
		long dbTime = -1;
		String exception = null;
		TableMetaInfo emi = loadEntityMetaInfo(entity.getClass());
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + entity.getClass() + "] not found! ");
		}
		if (tableName == null || tableName.equals("")) {
			tableName = emi.getTableName();
		}

		if (connName == null || connName.equals("")) {
			connName = DMConfigManager.getRouteMapping(tableName, "write");
		}
		StringBuilder sb = new StringBuilder();
		ArrayList<String> cols = new ArrayList<String>(entity.GET_UPDATED_COLUMN());
		List<FieldMetaInfo> pks = emi.getPklist();
		sb.append("update ").append(tableName).append(" set ");
		for (String col : cols) {
			sb.append(col).append("=?,");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" where ");
		for (int i = 0; i < pks.size(); i++) {
			FieldMetaInfo fmi = pks.get(i);
			sb.append(fmi.getColumnName()).append("=? ");
			if (i > 0) {
				sb.append("and ");
			}
		}

		Connection con = null;
		PreparedStatement pstmt = null;
		int effect = 0;
		try {
			con = dao.getTransactionController().getConnection(connName);
			pstmt = dao.getBatchUpdateController().prepareStatement(con, sb.toString());

			int seq = 0;
			for (String col : cols) {
				FieldMetaInfo fmi = emi.getFieldMetaInfo(col);
				if (fmi == null) {
					throw new TransactionException("FieldMetaInfo[" + col + "@" + entity.getClass() + "] not found! ");
				}
				DmReflectUtils.DAOLiteSaveReflect(pstmt, entity, fmi, ++seq);
			}
			// 开始where主键。
			for (FieldMetaInfo fmi : pks) {
				DmReflectUtils.DAOLiteSaveReflect(pstmt, entity, fmi, ++seq);
			}
			long dbStart = System.currentTimeMillis();
			effect = pstmt.executeUpdate();
			dbTime = System.currentTimeMillis() - dbStart;

		} catch (Exception e) {
			exception = e.toString();
			throw new TransactionException("TransactionException in DAOCommandImpl.java:update()", e);
		} finally {
			if (!dao.getBatchUpdateController().getBatchStatus() && con != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dao.getTransactionController().isAutoCommit() && con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, sb.toString(), entity.GET_UPDATED_INFO(), effect, dbTime, allTime, exception);
		return effect;
	}

	/**
	 * 删除一个实体.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名
	 * @param entity 实体类
	 * @param tableName 表名
	 * @return int
	 * @throws TransactionException 事务异常
	 */
	public static int delete(DAOFactoryImpl dao, String connName, DataEntity entity, String tableName) throws TransactionException {
		long start = System.currentTimeMillis();
		long dbTime = -1;
		String exception = null;
		TableMetaInfo emi = loadEntityMetaInfo(entity.getClass());
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + entity.getClass() + "] not found! ");
		}
		if (tableName == null || tableName.equals("")) {
			tableName = emi.getTableName();
		}

		if (connName == null || connName.equals("")) {
			connName = DMConfigManager.getRouteMapping(tableName, "write");
		}

		StringBuilder sb = new StringBuilder();
		List<FieldMetaInfo> pks = emi.getPklist();
		sb.append("delete from ").append(tableName);
		sb.append(" where ");
		for (int i = 0; i < pks.size(); i++) {
			FieldMetaInfo fmi = pks.get(i);
			sb.append(fmi.getColumnName()).append("=? ");
			if (i > 0) {
				sb.append("and ");
			}
		}

		Connection con = null;
		PreparedStatement pstmt = null;
		int effect = 0;
		try {
			con = dao.getTransactionController().getConnection(connName);
			pstmt = dao.getBatchUpdateController().prepareStatement(con, sb.toString());
			int seq = 0;
			// 开始where主键。
			for (FieldMetaInfo fmi : pks) {
				DmReflectUtils.DAOLiteSaveReflect(pstmt, entity, fmi, ++seq);
			}
			long dbStart = System.currentTimeMillis();
			effect = pstmt.executeUpdate();
			dbTime = System.currentTimeMillis() - dbStart;
		} catch (Exception e) {
			exception = e.toString();

			throw new TransactionException("TransactionException in DAOCommandImpl.java:delete()", e);
		} finally {
			if (!dao.getBatchUpdateController().getBatchStatus() && con != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dao.getTransactionController().isAutoCommit() && con != null) {
				try {
					con.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, sb.toString(), "", effect, dbTime, allTime, exception);
		return effect;
	}

	/**
	 * 获得列表.
	 * @param dao DAOFactoryImpl对象
	 * @param connName 连接名
	 * @param cls 要映射的对象类型
	 * @param selectsql 查询SQL语句
	 * @param paramList 参数的Object数组
	 * @param startIndex 开始位置
	 * @param resultNum 结果集大小
	 * @param autoCount 是否统计全部数据（用于分页算法），默认为false。
	 * @param <T> 要映射的对象类型
	 * @return 列表
	 * @throws TransactionException 事务异常
	 */
	public static <T> DataList<T> list(DAOFactoryImpl dao, String connName, Class<T> cls, String selectsql, Object[] paramList, int startIndex, int resultNum, boolean autoCount) throws TransactionException {
		long start = System.currentTimeMillis();
		long dbTime = -1;
		String exception = null;
		if (connName == null) {
			connName = SQLUtils.getConnNameFromSQL(selectsql);
		}
		Connection con = null;
		PreparedStatement pstmt = null;
		Object[] po = null;
		TableMetaInfo emi = loadEntityMetaInfo(cls);
		if (emi == null) {
			throw new TransactionException("TableMetaInfo[" + cls.getName() + "] not found! ");
		}

		int allsize = 0;

		if (autoCount) {
			String countsql = "select count(1) from (" + selectsql + ") must_alias";
			allsize = SQLCommandImpl.selectForSingleValue(dao, connName, int.class, countsql, paramList);
		}

		List<T> list = new ArrayList<T>();

		try {
			con = dao.getTransactionController().getConnection(connName);
			if (resultNum > 0 && startIndex >= 0) {
				Dialect dialect = DialectManager.getDialect(DMConfigManager.getConnPoolConfig(connName).getDbType());
				po = dialect.getPagedSQL(selectsql, startIndex, resultNum);
				selectsql = po[0].toString();
			}

			pstmt = con.prepareStatement(selectsql);
			int i = 0;

			if (paramList != null && paramList.length > 0) {
				for (i = 0; i < paramList.length; i++) {
					DmReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
				}
			}

			if (resultNum > 0 && startIndex >= 0) {
				pstmt.setInt(i + 1, (Integer) po[1]);
				pstmt.setInt(i + 2, (Integer) po[2]);
			}
			long dbStart = System.currentTimeMillis();
			ResultSet rs = pstmt.executeQuery();
			dbTime = System.currentTimeMillis() - dbStart;

			// 获得字段列表
			ResultSetMetaData rsm = rs.getMetaData();
			int colsCount = rsm.getColumnCount();
			String[] cols = new String[colsCount];
			for (int k = 0; k < colsCount; k++) {
				cols[k] = rsm.getColumnLabel(k + 1).toLowerCase();
			}

			while (rs.next()) {
				T entity = cls.newInstance();
				for (String col : cols) {
					FieldMetaInfo fmi = emi.getFieldMetaInfo(col);
					if (fmi != null) {
						DmReflectUtils.DAOLiteLoadReflect(rs, entity, fmi);
					}
				}
				list.add(entity);
			}

		} catch (Exception e) {
			exception = e.toString();

			throw new TransactionException("TransactionException in EntityCommandImpl.list()", e);
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
		long allTime = System.currentTimeMillis() - start;
		dao.addSqlExecuteStats(connName, selectsql, Arrays.toString(paramList), list.size(), dbTime, allTime, exception);
		return new DataList<T>(list, startIndex, resultNum, allsize);
	}

	/**
	 * 获取表名.
	 * @param cls 类型
	 * @return 表名
	 */
	static String getTableName(Class<?> cls) {
		TableMetaInfo emi = loadEntityMetaInfo(cls);
		if (emi != null) {
			return emi.getTableName();
		} else {
			return null;
		}
	}

	/**
	 * 加载读取pojo的注解信息.
	 * @param entityCls 实体类类型
	 * @return TableMetaInfo对象
	 */
	static TableMetaInfo loadEntityMetaInfo(Class<?> entityCls) {

		TableMetaInfo emi = entityMetaCache.get(entityCls.getName());
		if (emi == null) {
			emi = new TableMetaInfo();
			if (entityCls.isAnnotationPresent(TableMeta.class)) {
				TableMeta tm = entityCls.getAnnotation(TableMeta.class);
				emi.setTableName(tm.tableName());
			}
			Class<?> clazz = entityCls;
			for(int i = 0; clazz != Object.class && i < MAX_ENTITY_CLASS_EXTEND_LEVEL;
                clazz = clazz.getSuperclass(),i++) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(ColumnMeta.class)) {
                        ColumnMeta meta = field.getAnnotation(ColumnMeta.class);
                        FieldMetaInfo fieldInfo = new FieldMetaInfo();
                        fieldInfo.setPropertyName(field.getName());
                        fieldInfo.setColumnName(meta.columnName());
                        fieldInfo.setPrimaryKey(meta.primaryKey());
                        fieldInfo.setField(field);
                        fieldInfo.setAutoIncrement(meta.autoIncrement());
                        if (fieldInfo.isPrimaryKey()) {
                            emi.addPklist(fieldInfo);
                        }
                        emi.addColumnMap(meta.columnName(), fieldInfo);
                    }
                }
            }
            entityMetaCache.put(entityCls.getName(), emi);
		}

		return emi;
	}

}
