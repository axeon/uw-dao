package uw.util;

/**
 * byte数组和hex转换的工具类。
 * 
 * @author axeon
 * 
 */
public class ByteHexUtils {

	/**
	 * 十进制转十六进制
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		char[] newChar = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			newChar[2 * i] = hex[(b[i] & 0xf0) >> 4];
			newChar[2 * i + 1] = hex[b[i] & 0xf];
		}
		return new String(newChar);
	}

	/**
	 * 十六进制字符串转十进制
	 * 
	 * @param hexString
	 * @return
	 */
	public static byte[] hex2byte(String hexString) {
		if (hexString.length() % 2 != 0) {
			throw new IllegalArgumentException("error");
		}
		char[] chars = hexString.toCharArray();
		byte[] b = new byte[chars.length / 2];
		for (int i = 0; i < b.length; i++) {
			int high = Character.digit(chars[2 * i], 16) << 4;
			int low = Character.digit(chars[2 * i + 1], 16);
			b[i] = (byte) (high | low);
		}
		return b;
	}

}
