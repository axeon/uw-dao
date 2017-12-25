package uw.dao.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 列的属性.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColumnMeta {

	/**
	 * 列名.
	 * 
	 * @return String 列名
	 */
	public String columnName() default "";

	/**
	 * 数据类型.
	 * 
	 * @return String 数据类型
	 */
	public String dataType() default "";

	/**
	 * 最大长度.
	 * 
	 * @return int 最大长度
	 */
	public int dataSize() default 0;

	/**
	 * 是否主键.
	 * 
	 * @return boolean 是否主键
	 */
	public boolean primaryKey() default false;

	/**
	 * 是否可以为空.
	 * 
	 * @return boolean 是否可以为空？
	 */
	public boolean nullable() default true;

	/**
	 * 是否是自动递增字段.
	 * 
	 * @return boolean 是否是自动递增字段？
	 */
	public boolean autoIncrement() default false;
}
