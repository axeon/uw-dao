package uw.dao.gencode;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * TableMeta接口.
 *
 * @author liliang
 * @since 2017/9/11
 */
public interface TableMetaInterface {

    /**
     * 获取数据库连接名.
     *
     * @return 连接名
     */
    String getConnName();

    /**
     * 获取数据库连接.
     *
     * @return Connection
     * @throws SQLException SQL异常
     */
    Connection getConnection() throws SQLException;

    /**
     * 获取生成表的元数据.
     *
     * @param tables 表集合
     * @return 生成表的元数据
     */
    List<MetaTableInfo> getTablesAndViews(Set<String> tables);

    /**
     * 获取生成表的列数据.
     *
     * @param tableName 表名
     * @param pkList    MetaPrimaryKeyInfo集合
     * @return 生成表的列数据
     */
    List<MetaColumnInfo> getColumnList(String tableName, List<MetaPrimaryKeyInfo> pkList);

    /**
     * 获取生成表的主键数据.
     *
     * @param tableName 表名
     * @return 生成表的主键数据
     */
    List<MetaPrimaryKeyInfo> getPrimaryKey(String tableName);
}
