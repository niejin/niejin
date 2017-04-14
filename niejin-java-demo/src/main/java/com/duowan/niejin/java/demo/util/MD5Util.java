package com.duowan.niejin.java.demo.util;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author N.Jin{@link niejin@yy.com}
 * @Time 2017年4月5日
 *
 **/
public class MD5Util {
	private static final ThreadLocal<MessageDigest> messageDigestHolder = new ThreadLocal<MessageDigest>();

	static {
		try {
			MessageDigest message = MessageDigest.getInstance("MD5");
			messageDigestHolder.set(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 用来将字节转换成 16 进制表示的字符
	private static final char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static String md5(String data) {
		try {
			MessageDigest md5 = messageDigestHolder.get();
			if (md5 == null) {
				md5 = MessageDigest.getInstance("MD5");
				messageDigestHolder.set(md5);
			}

			md5.update(data.getBytes("UTF-8"));

			byte[] b = md5.digest();

			/*	
		 	StringBuilder digestHex = new StringBuilder(32);
			for (int i = 0; i < 16; i++) {
				digestHex.append(byteHEX(b[i]));
			}
			return digestHex.toString();
			*/
			 return new BigInteger(1,b).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteHEX(byte ib) {
		char[] ob = new char[2];
		ob[0] = hexDigits[(ib >>> 4) & 0X0F];
		ob[1] = hexDigits[ib & 0X0F];
		return new String(ob);
	}
}
