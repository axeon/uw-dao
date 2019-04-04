package uw.dao.dialect;

/**
 * 方言基类,对于当前的情况，可能只有分页需要处理.
 *
 * @author axeon
 */
public class Dialect {

    /**
     * 获得分页sql.
     *
     * @param sql       执行sql
     * @param startPos  起始位置
     * @param resultNum 结果集大小
     * @return null
     */
    public Object[] getPagedSQL(String sql, int startPos, int resultNum) {
        return null;
    }

}
