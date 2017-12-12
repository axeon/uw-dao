package uw.dm.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 实体信息.
 * 
 * @author axeon
 */
public class TableMetaInfo {

	/**
	 * 表名.
	 */
	private String tableName;

	/**
	 * 主键列表.
	 */
	private List<FieldMetaInfo> pklist = new ArrayList<FieldMetaInfo>();

	/**
	 * 列名列表. key=column.
	 */
	private Map<String, FieldMetaInfo> columnMap = new LinkedHashMap<String, FieldMetaInfo>();

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
	 * @return the pklist
	 */
	public List<FieldMetaInfo> getPklist() {
		return pklist;
	}

	/**
	 * @param fi
	 *            the pklist to set
	 */
	public void addPklist(FieldMetaInfo fi) {
		this.pklist.add(fi);
	}

	/**
	 * @return the columnMap
	 */
	public Map<String, FieldMetaInfo> getColumnMap() {
		return columnMap;
	}

	/**
	 * 根据columnName获取FieldMetaInfo.
	 * 
	 * @param columnName
	 *            列名
	 * @return FieldMetaInfo集合
	 */
	public FieldMetaInfo getFieldMetaInfo(String columnName) {
		return columnMap.get(columnName);
	}

	/**
	 * 向ColumnMap中加入FieldInfo信息.
	 * 
	 * @param columnName
	 *            列名
	 * @param fi
	 *            FieldMetaInfo对象
	 */
	public void addColumnMap(String columnName, FieldMetaInfo fi) {
		this.columnMap.put(columnName, fi);
	}

}
