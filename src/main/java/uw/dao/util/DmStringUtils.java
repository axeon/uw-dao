package uw.dao.util;

/**
 * dm的字符串工具类.
 * 
 * @author axeon
 */
public class DmStringUtils {

	/**
	 * 把_分割的字符串改为驼峰样式.
	 * 
	 * @param text
	 *            字符串
	 * @return String
	 */
	public static String toClearCase(String text) {
		StringBuilder sb = new StringBuilder();
		char[] data = text.toCharArray();
		boolean needClear = false;
		for (int i = 0; i < data.length; i++) {
			if (data[i] != '_') {
				if (needClear) {
					sb.append(Character.toUpperCase(data[i]));
					needClear = false;
				} else {
					sb.append(data[i]);
				}
			} else {
				needClear = true;
			}
		}
		return sb.toString();
	}

	/**
	 * 首字母大写.
	 * 
	 * @param text
	 *            字符串
	 * @return String
	 */
	public static String toUpperFirst(String text) {
		char[] ch = text.toCharArray();
		if (ch[0] >= 'a' && ch[0] <= 'z') {
			ch[0] -= 32;
		}
		return new String(ch);
	}

	/**
	 * 首字母大写.
	 * 
	 * @param text
	 *            字符串
	 * @return String
	 */
	public static String toLowerFirst(String text) {
		char[] ch = text.toCharArray();
		if (ch[0] >= 'A' && ch[0] <= 'Z') {
			ch[0] += 32;
		}
		return new String(ch);
	}

}
