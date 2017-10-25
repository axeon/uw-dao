package uw.dm.gencode;

/**
 * 表的列信息.
 * @author axeon
 */
public class MetaColumnInfo {

	/**
	 * 列名.
	 */
	private String columnName;

	/**
	 * 属性名.
	 */
	private String propertyName;

	/**
	 * 属性类型.
	 */
	private String PropertyType;

	/**
	 * 字段数据类型(对应java.sql.Types中的常量).
	 */
	private int dataType;

	/**
	 * 字段类型名称(例如：VACHAR2).
	 */
	private String typeName;

	/**
	 * 列的大小.
	 */
	private int columnSize;

	/**
	 * 描述列的注释.
	 */
	private String remarks;

	/**
	 * 确定列是否包括 null.
	 */
	private String isNullable;

	/**
	 * 确定列是否包括 null.
	 */
	private String isAutoIncrement;

	/**
	 * 是否是主键.
	 */
	private String isPrimaryKey;

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
	 * @return the propertyType
	 */
	public String getPropertyType() {
		return PropertyType;
	}

	/**
	 * @param propertyType
	 *            the propertyType to set
	 */
	public void setPropertyType(String propertyType) {
		PropertyType = propertyType;
	}

	/**
	 * @return the isPrimaryKey
	 */
	public String getIsPrimaryKey() {
		return isPrimaryKey;
	}

	/**
	 * @param isPrimaryKey
	 *            the isPrimaryKey to set
	 */
	public void setIsPrimaryKey(String isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	/**
	 * 转化成字符串形式.
	 * @return String
	 */
	public String toString() {
		return "MetaColumnInfo:" + columnName + "," + dataType + "," + typeName + "," + columnSize + "," + remarks + "," + isNullable + "," + isAutoIncrement;
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
	 * @return the dataType
	 */
	public int getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            the dataType to set
	 */
	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * @param typeName
	 *            the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @return the columnSize
	 */
	public int getColumnSize() {
		return columnSize;
	}

	/**
	 * @param columnSize
	 *            the columnSize to set
	 */
	public void setColumnSize(int columnSize) {
		this.columnSize = columnSize;
	}

	/**
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks
	 *            the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	/**
	 * @return the isNullable
	 */
	public String getIsNullable() {
		return isNullable;
	}

	/**
	 * @param isNullable
	 *            the isNullable to set
	 */
	public void setIsNullable(String isNullable) {
		this.isNullable = isNullable;
	}

	/**
	 * @return the isAutoIncrement
	 */
	public String getIsAutoIncrement() {
		return isAutoIncrement;
	}

	/**
	 * @param isAutoIncrement
	 *            the isAutoIncrement to set
	 */
	public void setIsAutoIncrement(String isAutoIncrement) {
		this.isAutoIncrement = isAutoIncrement;
	}

}
