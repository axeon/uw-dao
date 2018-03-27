package uw.dao.dialect;

/**
 * mysql方言.
 * 
 * @author axeon
 */
public class MySQLDialect extends Dialect {

	/**
	 * 获得分页sql.
	 * 
	 * @param sql
	 *            执行sql
	 * @param startPos
	 *            起始位置
	 * @param resultNum
	 *            结果集大小
	 * @return
	 */
	@Override
	public Object[] getPagedSQL(String sql, int startPos, int resultNum) {
		return new Object[] { sql + " limit ?,?", startPos, resultNum };
	}

}
