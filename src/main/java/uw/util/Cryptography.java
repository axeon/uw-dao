package uw.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基础加密算法类。当前支持des3,md5。
 * 
 * @author zhangjin
 * 
 */
public class Cryptography {

	private static final Logger logger = LoggerFactory.getLogger(Cryptography.class);
	


	/**
	 * 解密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String DES3Decrypt(String data, String key) {
		return DES3Decrypt(data, key, "UTF-8");
	}

	/**
	 * 解密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String DES3Decrypt(String data, String key, String encoding) {
		String edata = data;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes(encoding));
			DESedeKeySpec dks = new DESedeKeySpec(convert16To24(digest.digest()));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
			SecretKey securekey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, securekey, new SecureRandom());
			edata = new String(cipher.doFinal(BASE64Decrypt(data)), encoding);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return edata;
	}

	/**
	 * 加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String DES3Encrypt(String data, String key) {
		return DES3Encrypt(data, key, "UTF-8");
	}

	/**
	 * 加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String DES3Encrypt(String data, String key, String encoding) {
		String edata = data;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(key.getBytes(encoding));
			DESedeKeySpec dks = new DESedeKeySpec(convert16To24(digest.digest()));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
			SecretKey securekey = keyFactory.generateSecret(dks);
			Cipher cipher = Cipher.getInstance("DESede/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, securekey, new SecureRandom());
			edata = BASE64Encrypt(cipher.doFinal(data.getBytes(encoding)));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

		}
		return edata;
	}

	/**
	 * 把16位凑成24位数组。
	 * 
	 * @param okey
	 * @return
	 */
	private static byte[] convert16To24(byte[] okey) {
		byte[] dkey = new byte[24];
		for (int i = 0; i < dkey.length; i++) {
			dkey[i] = okey[i % 16];
		}
		return dkey;
	}
	
	/**
	 * MD5加密
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public static final String MD5Encrypt(String data) {
		String edata = data;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(data.getBytes("UTF-8"));
			edata = byte2hex(digest.digest()).toLowerCase();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return edata;
	}

	/**
	 * BASE64解密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] BASE64Decrypt(String data) {
		byte[] retdata = null;
		try {
			retdata = Base64.getDecoder().decode(data);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return retdata;
	}

	/**
	 * BASE64加密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String BASE64Encrypt(byte[] key) {
		String retdata = null;
		try {
			retdata = Base64.getEncoder().encodeToString(key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return retdata;
	}

	/**
	 * 简单加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String simpleEncrypt(String data, String key) {
		return simpleEncrypt(data, key, "UTF-8");
	}

	/**
	 * 简单加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String simpleEncrypt(String data, String key, String encoding) {
		byte[] sourceByte = null;
		byte[] keyByte = null;
		try {
			sourceByte = data.getBytes(encoding);
			keyByte = key.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		if (sourceByte == null || keyByte == null) {
			return null;
		}
		int sourceCount = sourceByte.length;
		int keyCount = keyByte.length;
		for (int i = 0; i < sourceCount; i++)
			sourceByte[i] ^= keyByte[i % keyCount];
		return BASE64Encrypt(sourceByte);
	}

	/**
	 * 简单加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String simpleDecrypt(String data, String key) {
		return simpleDecrypt(data, key, "UTF-8");
	}

	/**
	 * 简单加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public static String simpleDecrypt(String data, String key, String encoding) {
		byte[] sourceByte = null;
		byte[] keyByte = null;
		try {
			keyByte = key.getBytes(encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		sourceByte = BASE64Decrypt(data);
		if (sourceByte == null || keyByte == null) {
			return null;
		}
		int sourceCount = sourceByte.length;
		int keyCount = keyByte.length;
		for (int i = 0; i < sourceCount; i++)
			sourceByte[i] ^= keyByte[i % keyCount];
		String source = null;
		try {
			return new String(sourceByte, encoding);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}
		return source;
	}

	/**
	 * 字符串编码 对字符串进行快速扰乱，速度快。
	 * 
	 * @param input
	 * @return
	 */
	public final static String encodeString(String input) {
		int sum = 0;
		int count = input.length();
		int mid = count / 2;// 中点数值，因为要转换为数组下标，所以-1
		boolean ismod = (count % 2 == 0);// 是否可以整除
		char[] sourceChar = input.toCharArray();
		// 计算sum数值
		for (char c : sourceChar) {
			sum += c;
		}
		int rand = sum % 7 + 1;// 除7的余数+1作为随机种子
		// 重排字符串，每8个字符，以rand为中点交换位置。
		char[] encChar1 = new char[count];
		for (int i = 0; i < sourceChar.length;) {
			if (i + 8 < sourceChar.length) {
				// 填充从0-rand
				System.arraycopy(sourceChar, i, encChar1, i + rand, 8 - rand);
				// 填充rand-8
				System.arraycopy(sourceChar, i + 8 - rand, encChar1, i, rand);
			} else {
				System.arraycopy(sourceChar, i, encChar1, i, sourceChar.length - i);
			}
			i += 8;
		}
		// 中值重排
		char[] encChar2 = new char[count];
		int j = 0;
		if (ismod)
			mid--;
		encChar2[j++] = encChar1[mid];
		for (int i = 1; i <= mid; i++) {
			encChar2[j++] = encChar1[mid + i];
			encChar2[j++] = encChar1[mid - i];
		}
		if (ismod) {
			encChar2[j++] = encChar1[count - 1];
		}
		return new StringBuilder(sourceChar.length + 15).append(encChar2).append('.').append(sum).toString();
	}

	/**
	 * 字符串解码。
	 * 
	 * @param input
	 * @return
	 */
	public final static String decodeString(String input) {
		int sum = 0, checksum = 0;
		int p = input.lastIndexOf(".");
		if (p < 0) {
			return null;
		}
		try {
			sum = Integer.parseInt(input.substring(p + 1));
		} catch (Exception e) {
			return null;
		}
		input = input.substring(0, p);
		int count = input.length();
		boolean ismod = (count % 2 == 0);// 是否可以整除
		char[] sourceChar = input.toCharArray();
		char[] encChar1 = new char[count];
		int j = 0;
		if (ismod)
			count--;
		for (int i = count - 1; i >= 0; i = i - 2) {
			checksum += encChar1[j++] = sourceChar[i];
		}
		for (int i = 1; i < count; i = i + 2) {
			checksum += encChar1[j++] = sourceChar[i];
		}
		if (ismod) {
			checksum += encChar1[j++] = sourceChar[count];
		}
		int rand = sum % 7 + 1;// 除7的余数+1作为随机种子
		count = input.length();
		// 重排字符串，每8个字符，以rand为中点交换位置。
		char[] encChar2 = new char[count];
		for (int i = 0; i < sourceChar.length;) {
			if (i + 8 < sourceChar.length) {
				// 填充从0-rand
				System.arraycopy(encChar1, i + rand, encChar2, i, 8 - rand);
				// 填充rand-8
				System.arraycopy(encChar1, i, encChar2, i + 8 - rand, rand);
			} else {
				System.arraycopy(encChar1, i, encChar2, i, encChar2.length - i);
			}
			i += 8;
		}
		// 校验sum
		if (sum == checksum)
			return new String(encChar2);
		else
			return null;
	}

	/**
	 * 二行制转字符串
	 * 
	 * @param b
	 * @return
	 */
	public static String byte2hex(byte[] b) {
		StringBuilder sb = new StringBuilder(b.length * 3);
		String stmp = null;
		for (int n = 0; n < b.length; n++) {
			stmp = Integer.toHexString(b[n] & 0XFF);
			if (stmp.length() == 1)
				sb.append('0');
			sb.append(stmp);
		}
		return sb.toString().toUpperCase();
	}

	public static byte[] hex2byte(String s) {
		byte[] b = s.getBytes();
		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

	/**
	 * 为了适应在web中传输，将一些会转义的字符修改为不转义字符
	 * 
	 * @param b64
	 * @return
	 */
	public static String boxBase64(String b64) {
		// 过滤换行符
		b64 = b64.replaceAll("\r", "");
		b64 = b64.replaceAll("\n", "");
		// 替换非标字符
		b64 = b64.replace('+', '-');
		b64 = b64.replace('/', '.');
		b64 = b64.replace('=', '*');
		return b64;
	}

	/**
	 * 将转以后的字符修改为转义前字符。
	 * 
	 * @param b64
	 * @return
	 */
	public static String unboxBase64(String b64) {
		b64 = b64.replace('-', '+');
		b64 = b64.replace('.', '/');
		b64 = b64.replace('*', '=');
		return b64;
	}

}
