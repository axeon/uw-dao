package uw.dm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 表的属性.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TableMeta {
	/**
	 * 表名.
	 * 
	 * @return 表名
	 */
	public String tableName() default "";

	/**
	 * 表类型.
	 * 
	 * @return 表类型
	 */
	public String tableType() default "";

}
