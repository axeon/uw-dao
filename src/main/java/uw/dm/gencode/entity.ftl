<#setting number_format="#">
package ${package};

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import uw.dm.DataEntity;
import uw.dm.annotation.ColumnMeta;
import uw.dm.annotation.TableMeta;

/**
 * ${tableMeta.entityName}实体类
 * <#if tableMeta.remarks??>${tableMeta.remarks}</#if>
 * 
 * @author ${author}
 * @version $Revision: 1.00 $ $Date: ${date?string("yyyy-MM-dd HH:mm:ss")}
 */
@TableMeta(tableName="${tableMeta.tableName}",tableType="${tableMeta.tableType}")
@ApiModel(value = "${tableMeta.tableName}的实体类", description = "${tableMeta.tableName}的实体类")
public class ${tableMeta.entityName?cap_first} implements DataEntity,Serializable{
	
	private static final long serialVersionUID = 1L;
	
<#list columnList as column>

	/**
	 * <#if column.remarks??>${column.remarks}</#if>
	 */
	@ColumnMeta(columnName="${column.columnName}", dataType="${column.propertyType}"<#if column.columnSize gt 0>, dataSize=${column.columnSize}</#if>, nullable=${column.isNullable}<#if column.isPrimaryKey??>, primaryKey=true</#if><#if column.isAutoIncrement??>, autoIncrement=true</#if>)
	@ApiModelProperty(value = "<#if column.remarks??>${column.remarks}</#if>", dataType="${column.propertyType}")
	private ${column.propertyType} ${column.propertyName};
</#list>

	/*
	 * 轻量级状态下更新列表list.
	 */
	public transient Set<String> _UPDATED_COLUMN = null;
		
	/*
	 * 更新的信息.
	 */
	private transient StringBuilder _UPDATED_INFO = null;

		
	/**
	 * 获得更改的字段列表.
	 */
	public Set<String> GET_UPDATED_COLUMN() {
		return _UPDATED_COLUMN;
	}

	/**
	 * 得到_UPDATED_INFO.
	 */
	public String GET_UPDATED_INFO() {
		if (this._UPDATED_INFO == null) {
			return null;
		} else {
			return this._UPDATED_INFO.toString();
		}
	}

	/**
	 * 清理_UPDATED_INFO和_UPDATED_COLUMN信息.
	 */
	public void CLEAR_UPDATED_INFO() {
		_UPDATED_COLUMN = null;
		_UPDATED_INFO = null;
	}
	
	/**
	 * 初始化set相关的信息.
	 */
	private void _INIT_UPDATE_INFO() {
		this._UPDATED_COLUMN = new HashSet<String>();
		this._UPDATED_INFO = new StringBuilder("表${tableMeta.tableName}主键\"" + <#list pkList as pk>this.${pk.propertyName}+ </#list>"\"更新为:\r\n");
	}	

<#list columnList as column>

	/**
	 * 获得<#if column.remarks??>${column.remarks}</#if>。
	 */
	public ${column.propertyType} get${column.propertyName?cap_first}(){
		return this.${column.propertyName};
	}
</#list>

<#list columnList as column>

	/**
	 * 设置<#if column.remarks??>${column.remarks}</#if>。
	 */
	public void set${column.propertyName?cap_first}(${column.propertyType} ${column.propertyName}){
		
		if ((!String.valueOf(this.${column.propertyName}).equals(String.valueOf(${column.propertyName})))) {
			if (this._UPDATED_COLUMN == null) {
				_INIT_UPDATE_INFO();
			}
			this._UPDATED_COLUMN.add("${column.columnName}");
			this._UPDATED_INFO.append("${column.columnName}:\"" + this.${column.propertyName}+ "\"=>\"" + ${column.propertyName} + "\"\r\n");
			this.${column.propertyName} = ${column.propertyName};
		}
	}
</#list>

	/**
	 * 重载toString方法.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
<#list columnList as column>
		sb.append("${column.columnName}:\"" + this.${column.propertyName} + "\"\r\n");
</#list>
		return sb.toString();
	}

}
