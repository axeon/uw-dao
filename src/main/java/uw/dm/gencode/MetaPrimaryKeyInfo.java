package uw.dm.gencode;

/**
 * 表的主键信息.
 * 
 * @author axeon
 *
 */
public class MetaPrimaryKeyInfo {

	/**
	 * 表名.
	 */
	private String tableName;

	/**
	 * 列名.
	 */
	private String columnName;

	/**
	 * 属性名.
	 */
	private String propertyName;

	/**
	 * 主键序列.
	 */
	private int keySeq;

	/**
	 * 主键名.
	 */
	private String pkName;

	/**
	 * 转化成字符串形式.
	 * 
	 * @return String
	 */
	public String toString() {
		return "MetaPrimaryKeyInfo:" + tableName + "," + columnName + "," + keySeq + "," + pkName;
	}

	/**
	 * @return the propertyName
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * @param propertyName
	 *            the propertyName to set
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * @param tableName
	 *            the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	/**
	 * @return the columnName
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @param columnName
	 *            the columnName to set
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @return the keySeq
	 */
	public int getKeySeq() {
		return keySeq;
	}

	/**
	 * @param keySeq
	 *            the keySeq to set
	 */
	public void setKeySeq(int keySeq) {
		this.keySeq = keySeq;
	}

	/**
	 * @return the pkName
	 */
	public String getPkName() {
		return pkName;
	}

	/**
	 * @param pkName
	 *            the pkName to set
	 */
	public void setPkName(String pkName) {
		this.pkName = pkName;
	}

}
