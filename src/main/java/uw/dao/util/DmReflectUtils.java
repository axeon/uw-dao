package uw.dao.util;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import uw.dao.vo.FieldMetaInfo;

/**
 * DM反射工具类.
 * 
 * @author zhangjin
 */
public class DmReflectUtils {

	/**
	 * 构造函数.
	 */
	private DmReflectUtils() {
	}

	/**
	 * 在preparedStatement中动态set数值.
	 * 
	 * @param pstmt
	 *            PreparedStatement
	 * @param entity
	 *            Object 类的实例
	 * @param fmi
	 *            列信息
	 * @param sequence
	 *            int 次序
	 * @throws Exception
	 *             异常
	 */
	public static final void DAOLiteSaveReflect(PreparedStatement pstmt, Object entity, FieldMetaInfo fmi, int sequence)
			throws Exception {
		Field fd = fmi.getField();
		Class<?> cls = fd.getType();
		if (cls == int.class) {
			pstmt.setInt(sequence, fd.getInt(entity));
		} else if (cls == String.class) {
			pstmt.setObject(sequence, fd.get(entity));
		} else if (cls == long.class) {
			pstmt.setLong(sequence, fd.getLong(entity));
		} else if (cls == java.util.Date.class) {
			pstmt.setTimestamp(sequence, DmValueUtils.dateToTimestamp((java.util.Date) fd.get(entity)));
		} else if (cls == double.class) {
			pstmt.setDouble(sequence, fd.getDouble(entity));
		} else if (cls == float.class) {
			pstmt.setFloat(sequence, fd.getFloat(entity));
		} else if (cls == short.class) {
			pstmt.setShort(sequence, fd.getShort(entity));
		} else if (cls == byte.class) {
			pstmt.setByte(sequence, fd.getByte(entity));
		} else if (cls == boolean.class) {
			pstmt.setBoolean(sequence, fd.getBoolean(entity));
		} else {
			pstmt.setObject(sequence, fd.get(entity));
		}
	}

	/**
	 * 通用的反射更新方法.
	 * 
	 * @param pstmt
	 *            PreparedStatement对象
	 * @param sequence
	 *            序列
	 * @param value
	 *            数值
	 * @throws Exception
	 *             异常
	 */
	public static final void CommandUpdateReflect(PreparedStatement pstmt, int sequence, Object value)
			throws Exception {
		if (value instanceof java.util.Date) {
			pstmt.setTimestamp(sequence, DmValueUtils.dateToTimestamp((java.util.Date) value));
		} else {
			pstmt.setObject(sequence, value);
		}
	}

	/**
	 * 动态载入.
	 * 
	 * @param rs
	 *            结果集
	 * @param entity
	 *            实体类
	 * @param fmi
	 *            FieldMetaInfo对象
	 * @throws Exception
	 *             实体类
	 */
	public static final void DAOLiteLoadReflect(ResultSet rs, Object entity, FieldMetaInfo fmi) throws Exception {
		Field fd = fmi.getField();
		Class<?> cls = fd.getType();
		if (cls == int.class) {
			fd.setInt(entity, new Integer(rs.getInt(fmi.getColumnName())));
		} else if (cls == long.class) {
			fd.setLong(entity, rs.getLong(fmi.getColumnName()));
		} else if (cls == String.class) {
			fd.set(entity, DmValueUtils.nullToStr((String) rs.getString(fmi.getColumnName())));
		} else if (cls == Date.class) {
			fd.set(entity, rs.getTimestamp(fmi.getColumnName()));
		} else if (cls == double.class) {
			fd.setDouble(entity, rs.getDouble(fmi.getColumnName()));
		} else if (cls == float.class) {
			fd.setFloat(entity, rs.getFloat(fmi.getColumnName()));
		} else if (cls == short.class) {
			fd.setShort(entity, rs.getShort(fmi.getColumnName()));
		} else if (cls == byte.class) {
			fd.setByte(entity, rs.getByte(fmi.getColumnName()));
		} else if (cls == boolean.class) {
			fd.setBoolean(entity, rs.getBoolean(fmi.getColumnName()));
		} else {
			fd.set(entity, rs.getObject(fmi.getColumnName()));
		}

	}

}
