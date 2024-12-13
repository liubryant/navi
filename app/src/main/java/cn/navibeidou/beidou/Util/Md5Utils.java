package cn.navibeidou.beidou.Util;

import java.security.MessageDigest;

/**
 * Md5加密工具类
 * @author Jun
 * @date 2020年4月5日 上午2:10:59
 */
public class Md5Utils {
	
	/**
	 * 对字符串进行MD5加密
	 * @param 	s	原始字符串
	 * @return		MD5加密后字符串
	 */
	public static String md5(String s) {
		try {
			return new String(toHex(toByte(s)).getBytes("UTF-8"), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return s;
		}
	}
	
	/**
	 * 将16进制字符串转换成字节数组
	 * @param s
	 * @return
	 */
	private static byte[] toByte(String s) {
		MessageDigest algorithm;
		try {
			algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(s.getBytes("UTF-8"));
			byte[] messageDigest = algorithm.digest();
			return messageDigest;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将指定byte数组转换成16进制字符串
	 * @param hash
	 * @return
	 */
	private static final String toHex(byte hash[]) {
		if (hash == null) {
			return null;
		}
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if ((hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(hash[i] & 0xff, 16));
		}
		return buf.toString();
	}
	
}
