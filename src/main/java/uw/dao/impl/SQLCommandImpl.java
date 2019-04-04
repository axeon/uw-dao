package uw.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uw.dao.DataSet;
import uw.dao.TransactionException;
import uw.dao.connectionpool.ConnectionManager;
import uw.dao.dialect.Dialect;
import uw.dao.util.DaoReflectUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * 为了更为高效的执行数据库命令，是该类产生的根本原因。 具体使用请自行参照源代码.
 */
public class SQLCommandImpl {

    /**
     * 日志.
     */
    private static final Logger logger = LoggerFactory.getLogger(SQLCommandImpl.class);

    /**
     * 获得单个数值.
     *
     * @param dao       DAOFactoryImpl对象
     * @param connName  连接名，如设置为null，则根据sql语句或表名动态路由确定
     * @param selectSql 查询的SQL
     * @param cls       要映射的对象类型
     * @param paramList 查询SQL的绑定参数
     * @param <T>       要映射的对象类型
     * @return 单个数值
     * @throws TransactionException 事务异常
     */
    @SuppressWarnings("unchecked")
    public static final <T> Optional<T> selectForSingleValue(DAOFactoryImpl dao, String connName, Class<T> cls, String selectSql,
                                                             Object[] paramList) throws TransactionException {
        long start = System.currentTimeMillis();
        long connTime = 0, dbTime = 0;
        int connId = 0;
        String exception = null;
        if (connName == null) {
            connName = SQLUtils.getConnNameFromSQL(selectSql);
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        Object value = null;
        try {
            con = dao.getTransactionController().getConnection(connName);
            connId = con.hashCode();
            pstmt = con.prepareStatement(selectSql);
            if (paramList != null && paramList.length > 0) {
                for (int i = 0; i < paramList.length; i++) {
                    DaoReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
                }
            }
            connTime = System.currentTimeMillis() - start;
            long dbStart = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            dbTime = System.currentTimeMillis() - dbStart;
            if (rs.next()) {
                if (cls == int.class || cls == Integer.class) {
                    value = rs.getInt(1);
                } else if (cls == String.class) {
                    value = rs.getString(1);
                } else if (cls == long.class || cls == Long.class) {
                    value = rs.getLong(1);
                } else if (cls == Date.class) {
                    value = rs.getTimestamp(1);
                } else if (cls == double.class || cls == Double.class) {
                    value = rs.getDouble(1);
                } else if (cls == float.class || cls == Float.class) {
                    value = rs.getFloat(1);
                } else if (cls == short.class || cls == Short.class) {
                    value = rs.getShort(1);
                } else if (cls == byte.class || cls == Byte.class) {
                    value = rs.getByte(1);
                } else if (cls == boolean.class || cls == Boolean.class) {
                    value = rs.getBoolean(1);
                } else {
                    value = rs.getObject(1);
                }
            }
            rs.close();
        } catch (Exception e) {
            exception = e.toString();
            throw new TransactionException(connName + ": " + e.getMessage(), e);
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
            long allTime = System.currentTimeMillis() - start;
            dao.addSqlExecuteStats(connName, connId, selectSql, Arrays.toString(paramList), value == null ? 0 : 1, connTime, dbTime, allTime,
                    exception);
        }
        return Optional.ofNullable((T) value);
    }

    /**
     * 获得单列数据列表.
     *
     * @param dao       DAOFactoryImpl对象
     * @param connName  连接名，如设置为null，则根据sql语句或表名动态路由确定
     * @param selectSql 查询的SQL
     * @param cls       要映射的对象类型
     * @param <T>       要映射的对象类型
     * @param paramList 查询SQL的绑定参数
     * @return 单列数据列表
     * @throws TransactionException 事务异常
     */
    @SuppressWarnings("unchecked")
    public static final <T> List<T> selectForSingleList(DAOFactoryImpl dao, String connName, Class<T> cls,
                                                        String selectSql, Object[] paramList) throws TransactionException {
        long start = System.currentTimeMillis();
        long connTime = 0, dbTime = 0;
        int connId = 0;
        String exception = null;
        if (connName == null) {
            connName = SQLUtils.getConnNameFromSQL(selectSql);
        }
        Connection con = null;
        PreparedStatement pstmt = null;
        ArrayList<Object> list = new ArrayList<Object>();
        try {
            con = dao.getTransactionController().getConnection(connName);
            connId = con.hashCode();
            pstmt = con.prepareStatement(selectSql);
            int i = 0;
            if (paramList != null && paramList.length > 0) {
                for (i = 0; i < paramList.length; i++) {
                    DaoReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
                }
            }
            connTime = System.currentTimeMillis() - start;
            long dbStart = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            dbTime = System.currentTimeMillis() - dbStart;

            if (cls == int.class || cls == Integer.class) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            } else if (cls == String.class) {
                while (rs.next()) {
                    list.add(rs.getString(1));
                }
            } else if (cls == long.class || cls == Long.class) {
                while (rs.next()) {
                    list.add(rs.getLong(1));
                }
            } else if (cls == Date.class) {
                while (rs.next()) {
                    list.add(rs.getTimestamp(1));
                }
            } else if (cls == double.class || cls == Double.class) {
                while (rs.next()) {
                    list.add(rs.getDouble(1));
                }
            } else if (cls == float.class || cls == Float.class) {
                while (rs.next()) {
                    list.add(rs.getFloat(1));
                }
            } else if (cls == short.class || cls == Short.class) {
                while (rs.next()) {
                    list.add(rs.getShort(1));
                }
            } else if (cls == byte.class || cls == Byte.class) {
                while (rs.next()) {
                    list.add(rs.getByte(1));
                }
            } else if (cls == boolean.class || cls == Boolean.class) {
                while (rs.next()) {
                    list.add(rs.getBoolean(1));
                }
            } else {
                while (rs.next()) {
                    list.add(rs.getObject(1));
                }
            }
            rs.close();
        } catch (Exception e) {
            exception = e.toString();
            throw new TransactionException(connName + ": " + e.getMessage(), e);
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
            long allTime = System.currentTimeMillis() - start;
            dao.addSqlExecuteStats(connName, connId, selectSql, Arrays.toString(paramList), list.size(), connTime, dbTime, allTime,
                    exception);
        }
        return (ArrayList<T>) list;
    }

    /**
     * 获得以DataSet为结果的数据集合.
     *
     * @param dao        DAOFactoryImpl对象
     * @param connName   连接名，如设置为null，则根据sql语句或表名动态路由确定
     * @param selectSql  查询的SQL
     * @param paramList  查询SQL的绑定参数
     * @param startIndex 开始位置，默认为0
     * @param resultNum  结果集大小，默认为0，获取全部数据
     * @param autoCount  是否统计全部数据（用于分页算法），默认为false。
     * @return 数据集合
     * @throws TransactionException 事务异常
     */
    public static final DataSet selectForDataSet(DAOFactoryImpl dao, String connName, String selectSql,
                                                 Object[] paramList, int startIndex, int resultNum, boolean autoCount) throws TransactionException {
        long start = System.currentTimeMillis();
        long connTime = 0, dbTime = 0;
        int connId = 0;
        String exception = null;
        if (connName == null) {
            connName = SQLUtils.getConnNameFromSQL(selectSql);
        }
        int allsize = 0;
        if (autoCount) {
            String countsql = "select count(1) from (" + selectSql + ") must_alias";
            Optional<Integer> countOptional = SQLCommandImpl.selectForSingleValue(dao, connName, Integer.class, countsql, paramList);
            if (countOptional.isPresent()) {
                allsize = countOptional.get();
            }
        }

        DataSet ds = null;
        Connection con = null;
        PreparedStatement pstmt = null;
        Object[] po = null;
        try {
            con = dao.getTransactionController().getConnection(connName);
            connId = con.hashCode();
            if (resultNum > 0 && startIndex >= 0) {
                Dialect dialect = ConnectionManager.getDialect(connName);
                po = dialect.getPagedSQL(selectSql, startIndex, resultNum);
                selectSql = po[0].toString();
            }

            pstmt = con.prepareStatement(selectSql);
            int i = 0;
            if (paramList != null && paramList.length > 0) {
                for (i = 0; i < paramList.length; i++) {
                    DaoReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
                }
            }
            if (resultNum > 0 && startIndex >= 0) {
                pstmt.setInt(i + 1, (Integer) po[1]);
                pstmt.setInt(i + 2, (Integer) po[2]);
            }
            connTime = System.currentTimeMillis() - start;
            long dbStart = System.currentTimeMillis();
            ResultSet rs = pstmt.executeQuery();
            dbTime = System.currentTimeMillis() - dbStart;
            ds = new DataSet(rs, startIndex, resultNum, allsize);
            rs.close();
        } catch (Exception e) {
            exception = e.toString();
            throw new TransactionException(connName + ": " + e.getMessage(), e);
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
            long allTime = System.currentTimeMillis() - start;
            dao.addSqlExecuteStats(connName, connId, selectSql, Arrays.toString(paramList), ds.size(), connTime, dbTime, allTime, exception);
        }
        return ds;
    }

    /**
     * 执行任意sql.
     *
     * @param dao        DAOFactoryImpl对象
     * @param connName   连接名，如设置为null，则根据sql语句或表名动态路由确定
     * @param executesql 执行的SQL
     * @param paramList  执行SQL的绑定参数
     * @return int
     * @throws TransactionException 事务异常
     */
    public static final int executeSQL(DAOFactoryImpl dao, String connName, String executesql, Object[] paramList)
            throws TransactionException {
        long start = System.currentTimeMillis();
        long connTime = 0, dbTime = 0;
        int connId = 0;
        String exception = null;

        if (connName == null) {
            connName = SQLUtils.getConnNameFromSQL(executesql);
        }

        Connection con = null;
        PreparedStatement pstmt = null;
        int effect = 0;
        try {
            con = dao.getTransactionController().getConnection(connName);
            connId = con.hashCode();
            pstmt = dao.getBatchUpdateController().prepareStatement(con, executesql);
            if (paramList != null && paramList.length > 0) {
                for (int i = 0; i < paramList.length; i++) {
                    DaoReflectUtils.CommandUpdateReflect(pstmt, i + 1, paramList[i]);
                }
            }
            connTime = System.currentTimeMillis() - start;
            long dbStart = System.currentTimeMillis();
            if (dao.getBatchUpdateController().getBatchStatus()) {
                pstmt.addBatch();
            } else {
                effect = pstmt.executeUpdate();
            }
            dbTime = System.currentTimeMillis() - dbStart;
        } catch (Exception e) {
            exception = e.toString();
            throw new TransactionException(connName + ": " + e.getMessage(), e);
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
            long allTime = System.currentTimeMillis() - start;
            dao.addSqlExecuteStats(connName, connId, executesql, Arrays.toString(paramList), effect, connTime, dbTime, allTime, exception);
        }
        return effect;
    }

}
