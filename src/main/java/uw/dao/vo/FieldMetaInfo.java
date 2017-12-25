package uw.dao.vo;

import java.lang.reflect.Field;

/**
 * 实体属性信息.
 * 
 * @author axeon
 */
public class FieldMetaInfo {

	/**
	 * java属性名.
	 */
	private String propertyName;

	/**
	 * 数据库字段名.
	 */
	private String columnName;

	/**
	 * 是否是主键.
	 */
	private boolean primaryKey = false;

	/**
	 * 是否是自动递增字段.
	 */
	private boolean autoIncrement = false;

	/**
	 * 属性反射句柄.
	 */
	private Field field;

	/**
	 * 是否是主键.
	 * 
	 * @return boolean
	 */
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	/**
	 * 设置是否是主键.
	 * 
	 * @param primaryKey
	 *            是或者否
	 */
	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	/**
	 * 获取java属性名.
	 * 
	 * @return java属性名
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * 设置java属性名.
	 * 
	 * @param propertyName
	 *            java属性名
	 */
	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	/**
	 * 获取数据库字段名.
	 * 
	 * @return 数据库字段名
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * 设置数据库字段名.
	 * 
	 * @param columnName
	 *            字段名
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * 获取属性反射句柄.
	 * 
	 * @return 属性反射句柄
	 */
	public Field getField() {
		return field;
	}

	/**
	 * 设置属性反射句柄.
	 * 
	 * @param field
	 *            属性反射句柄
	 */
	public void setField(Field field) {
		this.field = field;
	}

	/**
	 * @return the autoIncrement
	 */
	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	/**
	 * @param autoIncrement
	 *            the autoIncrement to set
	 */
	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

}