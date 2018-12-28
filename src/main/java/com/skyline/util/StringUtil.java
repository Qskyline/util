package com.skyline.util;

import org.apache.commons.lang3.StringUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class StringUtil {
	public static int compareVersion(Object version1, Object version2) {
		int[] __version1;
		int[] __version2;
		if (version1 instanceof int[] && version2 instanceof int[] && ((int[]) version1).length == ((int[]) version2).length) {
			__version1 = (int[]) version1;
			__version2 = (int[]) version2;
		} else if (version1 instanceof String && version2 instanceof String) {
			String[] _version1 = ((String) version1).split("\\.");
			String[] _version2 = ((String) version2).split("\\.");
			if (_version1.length != _version2.length) {
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

		if (__version1[0] > __version2[0]) return 1;
		else if (__version1[0] < __version2[0]) return -1;
		else if (__version1.length == 1) return 0;
		else {
			int length = __version1.length;
			int[] p_version1 = new int[length - 1];
			int[] p_version2 = new int[length - 1];
			for (int i = 1; i < length; i++) {
				p_version1[i - 1] = __version1[i];
				p_version2[i - 1] = __version2[i];
			}
			return compareVersion(p_version1, p_version2);
		}
	}

	public static String bytesToString(byte[] bytes, int len) {
		try {
			return StringUtils.trimToNull(new String(bytes, 0, len, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String bytesToString(byte[] bytes) {
		return bytesToString(bytes, bytes.length);
	}

	public static byte[] stringToBytes(String string, int len) {
		string = StringUtils.trimToNull(string);
		byte[] result = new byte[len];
		Arrays.fill(result, (byte) 0);
		if (string == null) return result;
		try {
			System.arraycopy(string.getBytes("UTF-8"), 0, result, 0, Math.min(string.getBytes("UTF-8").length, len));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		return result;
	}

	public static byte[] stringToBytes(String string) {
		try {
			return stringToBytes(string, string.getBytes("UTF-8").length);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getExceptionStackTraceMessage(Exception ex) {
		StringWriter sw = new StringWriter();
		PrintWriter writer = new PrintWriter(sw);
		ex.printStackTrace(writer);
		writer.flush();
		return sw.toString();
	}

	public static String trim(String args, char beTrim, int flag) {
		int st = 0;
		int len = args.length();
		char[] val = args.toCharArray();
		char sbeTrim = beTrim;
		if (flag == 0) {
			while ((st < len) && (val[st] == sbeTrim)) { st++; }
		} else if (flag == 1) {
			while ((len > st) && (val[len - 1] == sbeTrim)) { len--; }
		} else {
			while ((st < len) && (val[st] == sbeTrim)) { st++; }
			while ((len > st) && (val[len - 1] == sbeTrim)) { len--; }
		}
		return args.substring(st, len);
	}

	public static String trim(String args, char beTrim) {
		return trim(args, beTrim, 2);
	}

	public static boolean isIp(String ip) {
		String regex = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
		if (!ip.matches(regex)) return false;

		String[] data = ip.split("\\.");
		if(data.length != 4) return false;
		for (String seg : data) {
			if (seg.startsWith("0") && seg.length() > 1) return false;
			int t = Integer.valueOf(seg);
			if (t < 0 || t > 255) return false;
		}
		int first_seg = Integer.valueOf(data[0]);

		if (first_seg == 0 || first_seg == 255) return false;
		return true;
	}

	public static void main(String[] args) {
	}
}
