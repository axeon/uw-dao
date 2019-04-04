package uw.dao.dialect;

/**
 * oracle方言.
 *
 * @author axeon
 */
public class OracleDialect extends Dialect {

    /**
     * 获得分页sql.
     *
     * @param sql       执行sql
     * @param startPos  起始位置
     * @param resultNum 结果集大小
     * @return
     */
    @Override
    public Object[] getPagedSQL(String sql, int startPos, int resultNum) {
        return new Object[]{
                "select * from (select sub.*, rownum rnum from ( " + sql + " ) sub where rownum <= ?) where rnum > ?",
                startPos + resultNum, startPos};
    }

}
