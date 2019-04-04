package uw.dao.gencode;

/**
 * 数据库表的信息.
 *
 * @author axeon
 */
public class MetaTableInfo {

    /**
     * 表名.
     */
    private String tableName;

    /**
     * 实体名.
     */
    private String entityName;

    /**
     * 表类型.
     */
    private String tableType;

    /**
     * 备注.
     */
    private String remarks;

    /**
     * 转化成字符串形式.
     *
     * @return String
     */
    @Override
    public String toString() {
        return "MetaTableInfo:" + tableName + "," + tableType + "," + remarks;
    }

    /**
     * @return the entityName
     */
    public String getEntityName() {
        return entityName;
    }

    /**
     * @param entityName the entityName to set
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * @return the tableName
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName the tableName to set
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return the tableType
     */
    public String getTableType() {
        return tableType;
    }

    /**
     * @param tableType the tableType to set
     */
    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    /**
     * @return the remarks
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * @param remarks the remarks to set
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
