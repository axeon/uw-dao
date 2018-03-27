package uw.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * ip工具类。用于获取本机内外网ip.
 * 
 * @author zhangjin
 * 
 */
public class IPAddressUtils {

	private static final Logger logger = LoggerFactory.getLogger(IPAddressUtils.class);

	private static String internalHostAddress = "";

	private static String internalHostName = "";

	private static String internetHostAddress = "";

	private static String internetHostName = "";

	static {
		try {
			Enumeration<?> nie = NetworkInterface.getNetworkInterfaces();
			int i = 0;
			while (nie.hasMoreElements()) {
				NetworkInterface ni = (NetworkInterface) nie.nextElement();
				Enumeration<?> ase = ni.getInetAddresses();
				while (ase.hasMoreElements()) {
					InetAddress as = (InetAddress) ase.nextElement();
                    if (!(as instanceof Inet4Address)) {
						continue;
                    }
                    if (as.getHostAddress().equals("127.0.0.1") || as.getHostAddress().startsWith("169.254")) {
                        continue;
                    }
					if (i == 0) {
						internalHostName = internetHostName = as.getHostName();
						internalHostAddress = internetHostAddress = as.getHostAddress();
					} else {
						if (as.getHostAddress().startsWith("192.168") || as.getHostAddress().startsWith("10.0")) {
							internalHostAddress = as.getHostAddress();
							internalHostName = as.getHostName();
						} else {
							if (internetHostAddress.startsWith("192.168") || internetHostAddress.startsWith("10.0")) {
								internetHostAddress = as.getHostAddress();
								internetHostName = as.getHostName();
							}
						}
					}
					i++;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * 根据http请求头，请求ip地址。
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip.indexOf(",") != -1) {
			ip = ip.substring(ip.lastIndexOf(",") + 1, ip.length());
		}
		return ip;
	}

	/**
	 * 获得内网地址
	 * 
	 * @return
	 */
	public static String getInternalHostAddress() {
		return internalHostAddress;
	}

	/**
	 * 获得内网机器名
	 * 
	 * @return
	 */
	public static String getInternalHostName() {
		return internalHostName;
	}

	/**
	 * 获得外网地址
	 * 
	 * @return
	 */
	public static String getInternetHostAddress() {
		return internetHostAddress;
	}

	/**
	 * 获得外网机器名
	 * 
	 * @return
	 */
	public static String getInternetHostName() {
		return internetHostName;
	}

	/**
	 * 将字符串类型的ip转成long。
	 * 
	 * @param strIp
	 * @return
	 */
	public static long ipToLong(String strIp) {
		String[] ips = strIp.split("\\.");
		long[] ip = new long[4];
        if (ips.length != 4) {
            return -1;
        }
		try {
			ip[0] = Long.parseLong(ips[0]);
			ip[1] = Long.parseLong(ips[1]);
			ip[2] = Long.parseLong(ips[2]);
			ip[3] = Long.parseLong(ips[3]);

		} catch (Exception e) {
			ip = new long[4];
		}
		// 将每个.之间的字符串转换成整型
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

	/**
	 * 将十进制整数形式转换成127.0.0.1形式的ip地址
	 * 
	 * @param longIp
	 * @return
	 */
	public static String longToIp(long longIp) {
		StringBuffer sb = new StringBuffer("");
		// 直接右移24位
		sb.append(String.valueOf((longIp >>> 24)));
		sb.append(".");
		// 将高8位置0，然后右移16位
		sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
		sb.append(".");
		// 将高16位置0，然后右移8位
		sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
		sb.append(".");
		// 将高24位置0
		sb.append(String.valueOf((longIp & 0x000000FF)));
		return sb.toString();
	}

}
