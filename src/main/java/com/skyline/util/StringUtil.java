package com.skyline.util;

import java.util.Arrays;
import org.apache.commons.lang3.StringUtils;

public class StringUtil {
	public static int compareVersion(Object version1, Object version2) {
		int[] __version1 = null;
		int[] __version2 = null;	
		if(version1 instanceof int[] && version2 instanceof int[] && ((int[])version1).length == ((int[])version2).length) {
			__version1 = (int[])version1;
			__version2 = (int[])version2;
		} else if(version1 instanceof String && version2 instanceof String && ((String)version1).length() == ((String)version2).length()) {
			String[] _version1 = ((String)version1).split("\\.");
			String[] _version2 = ((String)version2).split("\\.");
			if(_version1.length != _version2.length) {
				return -2;
			} else {
				int length = _version1.length;
				__version1 = new int[length];
				__version2 = new int[length];
				for (int i = 0; i < length; i++) {
					try {
						__version1[i] = Integer.valueOf(_version1[i]);
						__version2[i] = Integer.valueOf(_version2[i]);
					} catch (Exception e) {
						return -2;
					}
				}
			}
		} else {
			return -2;
		}
		
		if(__version1[0] > __version2[0]) return 1;
		else if(__version1[0] < __version2[0]) return -1;
		else if(__version1.length == 1) return 0;
		else {
			int length = __version1.length;
			int[] p_version1 = new int[length-1];
			int[] p_version2 = new int[length-1];
			for (int i = 1; i < length; i++) {
				p_version1[i-1] = __version1[i];
				p_version2[i-1] = __version2[i];
			}
			return compareVersion(p_version1, p_version2);
		}
	}
	
	public static String bytesToString(byte[] bytes, int len) {
		return StringUtils.trimToNull(new String(bytes, 0, len));
	}
	
	public static String bytesToString(byte[] bytes) {
		return bytesToString(bytes, bytes.length);
	}
	
	public static byte[] stringToBytes(String string, int len) {
		string = StringUtils.trimToNull(string);
		byte[] result = new byte[len];
		Arrays.fill(result, (byte)0);
		if(string == null) return result;
		System.arraycopy(string.getBytes(), 0, result, 0, Math.min(string.length(), len));
		return result;
	}
	
	public static byte[] stringToBytes(String string) {
		string = StringUtils.trimToNull(string);
		return stringToBytes(string, string.length());
	}
	
	public static long bytesToLong(byte[] bytes) {
		long result = 0;
		int len = bytes.length;
		for (int i=len-1; i>=0; i--) {
			result = result | (((long)bytes[i]) << (8*(len-1-i)));
		}
		return result;
	}
	
	public static byte[] longToBytes(long data, int len) {
		byte[] result = new byte[len];
		Arrays.fill(result, (byte)0);
		for(int i=0; i<len; i++) {
			result[i] = (byte)((data >> (len-i-1)*8));
		}
		return result;
	}
	
	public static byte[] longToBytes(long data) {
		return longToBytes(data, 8);
	}
	
	public static void main(String[] args) {
		System.out.println(bytesToLong(new byte[] {(byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-1, (byte)-7}));
		byte[] aa = longToBytes(-7, 6);
		for (int i=0; i<aa.length; i++) {
			System.out.println((int)aa[i]);
		}
		System.out.println(bytesToLong(aa));
	}
}
