package uw.dm.impl;

import uw.dm.conf.DMConfigManager;

/**
 * SQL工具类.
 *
 * @author axeon
 */
public class SQLUtils {

	/**
	 * 从sql中获得连接信息.
	 * 
	 * @param sql
	 *            SQL语句
	 * @return 连接信息
	 */
	public static String getConnNameFromSQL(String sql) {
		sql = sql.trim().toLowerCase();
		String table = "", access = "all";
		sql = sql.replaceAll("\\s+", " "); // 替换所有空格
		if (sql.startsWith("select")) {
			String[] data = sql.split(" ");
			for (int i = 0; i < data.length; i++) {
				if (data[i].equals("from")) {
					if (data[i + 1].indexOf(',') > 0) {
						table = data[i + 1].split(",")[0];
					} else {
						table = data[i + 1];
					}
					break;
				}
			}
			access = "read";
		} else if (sql.startsWith("update")) {
			String[] data = sql.split(" ");
			table = data[1];
			access = "write";
		} else if (sql.startsWith("delete")) {
			String[] data = sql.split(" ");
			for (int i = 0; i < data.length; i++) {
				if (data[i].equals("from")) {
					if (data[i + 1].indexOf(',') > 0) {
						table = data[i + 1].split(",")[0];
					} else {
						table = data[i + 1];
					}
					break;
				}
			}
			access = "write";
		} else if (sql.startsWith("insert")) {
			String[] data = sql.split(" ");
			for (int i = 0; i < data.length; i++) {
				if (data[i].equals("into")) {
					table = data[i + 1];
					break;
				}
			}
			access = "write";
		} else if (sql.startsWith("replace") || sql.startsWith("merge")) {
			String[] data = sql.split(" ");
			for (int i = 0; i < data.length; i++) {
				if (data[i].equals("into")) {
					table = data[i + 1];
					break;
				}
			}
			access = "write";
		} else if (sql.startsWith("create")) {
			// 新建表create table
			String[] data = sql.split(" ");
			if (data[2].equals("if")) {
				// if not exists
				table = data[5];
			} else {
				table = data[2];
			}
			access = "write";
		}
		return DMConfigManager.getRouteMapping(table, access);
	}

}
