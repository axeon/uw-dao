package uw.dm.dialect;

/**
 * 方言基类,对于当前的情况，可能只有分页需要处理.
 * 
 * @author axeon
 */
public class DialectManager {

	/**
	 * 得到Dialect对象.
	 * 
	 * @param type
	 *            数据库类型
	 * @return Dialect对象
	 */
	public static Dialect getDialect(String type) {
		if ("mysql".equals(type)) {
			return new MySQLDialect();
		} else if ("oracle".equals(type)) {
			return new OracleDialect();
		} else {
			return new MySQLDialect();
		}
	}
}
