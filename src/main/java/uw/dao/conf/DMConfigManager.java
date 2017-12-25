package uw.dao.conf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import uw.dao.conf.DMConfig.ConnPoolConfig;
import uw.dao.conf.DMConfig.ConnRoute;
import uw.dao.conf.DMConfig.ConnRouteConfig;
import uw.dao.conf.DMConfig.TableShardingConfig;

/**
 * DM配置管理器.
 * 
 * @author axeon
 */
public class DMConfigManager {

	/**
	 * DM配置表.
	 */
	private static DMConfig config;

	/**
	 * 链接路由Map.
	 */
	private static Map<String, String> routeMap = new ConcurrentHashMap<>();

	/**
	 * @return the config
	 */
	public static DMConfig getConfig() {
		return config;
	}

	/**
	 * @param config
	 *            the config to set
	 */
	public static void setConfig(DMConfig config) {
		DMConfigManager.config = config;
	}

	/**
	 * 获得连接池配置列表.
	 *
	 * @return 连接池配置列表
	 */
	public static List<String> getConnPoolNameList() {
		ArrayList<String> list = new ArrayList<>();
		if (config.getConnPool() != null) {
			if (config.getConnPool().getList() != null) {
				list.addAll(config.getConnPool().getList().keySet());
			}
			if (config.getConnPool().getRoot() != null) {
				list.add("");
			}
		}
		return list;
	}

	/**
	 * 获得表分片配置.
	 *
	 * @param tableName
	 *            表名
	 * @return 表分片配置
	 */
	public static TableShardingConfig getTableShardingConfig(String tableName) {
		if (config == null || config.getTableSharding() == null) {
			return null;
		}
		return config.getTableSharding().get(tableName);
	}

	/**
	 * 获得连接池配置.
	 *
	 * @param name
	 * @return 连接池配置
	 */
	public static ConnPoolConfig getConnPoolConfig(String name) {
		ConnPoolConfig pool = null;
		if (config.getConnPool() != null && config.getConnPool().getList() != null) {
			pool = config.getConnPool().getList().get(name);
		}
		if (pool != null) {
			return pool;
		} else if (config.getConnPool() != null && (name == null || "".equals(name))) {
			return config.getConnPool().getRoot();
		} else {
			return null;
		}
	}

	/**
	 * 获得路由映射信息.
	 *
	 * @param table
	 *            表名
	 * @param access
	 *            权限
	 * @return 路由映射信息
	 */
	public static String getRouteMapping(String table, String access) {
		String key = table + "^" + access;
		String poolName = routeMap.get(key);
		if (poolName == null && config != null) {
			ConnRoute connRoute = config.getConnRoute();
			if (connRoute != null) {
				Map<String, ConnRouteConfig> map = connRoute.getList();
				// 先尝试匹配列表.
				if (map != null) {
					for (Entry<String, ConnRouteConfig> kv : map.entrySet()) {
						if (table.startsWith(kv.getKey())) {
							ConnRouteConfig route = kv.getValue();
							poolName = getPoolNameByAccess(route, access);
						}
					}
				}
				// 如果匹配不到，那么就直接从根配置获取.
				if (poolName == null) {
					ConnRouteConfig route = connRoute.getRoot();
					if (route != null) {
						poolName = getPoolNameByAccess(route, access);
					}
				}
			}
			// 如果还是没有找到，说明根本就没配置路由，直接返回默认链接.
			if (poolName == null) {
				poolName = "";
			}
			routeMap.put(key, poolName);
		}
		return poolName;
	}

	/**
	 * 通过route和access获取连接池名称.
	 *
	 * @param route
	 *            路由
	 * @param access
	 *            权限
	 * @return poolName
	 */
	private static String getPoolNameByAccess(ConnRouteConfig route, String access) {
		String poolName = null;
		if ("write".equalsIgnoreCase(access)) {
			poolName = route.getWrite();
		} else if ("read".equalsIgnoreCase(access)) {
			poolName = route.getRead();
		}
		// write read未配置则使用all的配置
		if (poolName == null) {
			poolName = route.getAll();
		}
		return poolName;
	}

}
