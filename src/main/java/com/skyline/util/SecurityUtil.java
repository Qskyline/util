package com.skyline.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

public class SecurityUtil {
	private static class DesEncrpt {
		private String charset = "utf-8";
		private final byte[] DESIV = new byte[] {0x12, 0x34, 0x56, 120, (byte)0x90, (byte)0xab, (byte)0xcd, (byte)0xef};   // 向量 
	    private AlgorithmParameterSpec iv = null;   // 加密算法的参数接口  
	    private Key key = null;
	    private Cipher cipher = null;
	    
	    public DesEncrpt(String desKey) throws Exception {
	        DESKeySpec keySpec = new DESKeySpec(desKey.getBytes(this.charset));   // 设置密钥参数  
	        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");   // 获得密钥工厂  
	        key = keyFactory.generateSecret(keySpec);// 得到密钥对象  
	        iv = new IvParameterSpec(DESIV);   // 设置向量
	        cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");   // 得到加密对象Cipher
	    }  
	    
	    public String encode(String data) throws Exception {
	    	cipher.init(Cipher.ENCRYPT_MODE, key, iv);
	        byte[] pasByte = cipher.doFinal(data.getBytes(this.charset));
	        return Base64.encodeBase64String(pasByte);
	    }
	    
	    public String decode(String data) throws Exception {
	    	cipher.init(Cipher.DECRYPT_MODE, key, iv);
	        byte[] pasByte = cipher.doFinal(Base64.decodeBase64(data));  
	        return new String(pasByte, "UTF-8");
	    }
	}
	
	public static String getFileMD5(String filePath) throws NoSuchAlgorithmException, IOException {
		File file = new File(filePath);
		if (!file.isFile()) {
			return null;
		}
		
		MessageDigest digest = null;
		digest = MessageDigest.getInstance("MD5");
		FileInputStream in = new FileInputStream(file);
		byte buffer[] = new byte[1024];
		int len;
		while ((len = in.read(buffer, 0, 1024)) != -1) {
			digest.update(buffer, 0, len);
		}
		in.close();
		BigInteger bigInt = new BigInteger(1, digest.digest());

		return bigInt.toString(16);
	}
	
	public static String Md5(String plainText) throws NoSuchAlgorithmException {
		StringBuffer buf = new StringBuffer(""); 
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(plainText.getBytes());
		byte b[] = md.digest();
		int i;
		for (int offset = 0; offset < b.length; offset++) {
			i = b[offset]; 
			if(i<0) i+= 256;
			if(i<16) buf.append("0"); 
			buf.append(Integer.toHexString(i));
		}
		return buf.toString();
	}
	
	public static String desEncrpt(String data, String desKey) throws Exception {
		return new DesEncrpt(desKey).encode(data);
	}
	
	public static String desDecrpt(String data, String desKey) throws Exception {
		return new DesEncrpt(desKey).decode(data);
	}
	
	public static String shaHex(String... data) {
		Arrays.sort(data);
		String join = StringUtils.join(data);
		String sign = DigestUtils.sha1Hex(join);
		return sign;
	}
	
	public static boolean checkUsername(String username) {
		if(username == null) return false;
		Pattern pattern = Pattern.compile("^[a-zA-Z][a-zA-Z|_|\\-]{3,19}$");
		Matcher matcher = pattern.matcher(username);
		return matcher.matches();
	}
	
	public static boolean checkPhoneNum(String phoneNum) {
		if(phoneNum == null) return false;
		Pattern pattern = Pattern.compile("^\\d{11}$");
		Matcher matcher = pattern.matcher(phoneNum);
		return matcher.matches();
	}
	
	public static void main(String[] args) {  
        try {
            String test = "9ba45bfd500642328ec03ad8ef1b6e75";
            String key = "c46b3d6667322fc092452a94afe4b";
            String tt = desEncrpt(test, key);
            System.out.println("加密前的字符：" + test);
            System.out.println("加密后的字符：" + tt);
            System.out.println("解密后的字符：" + desDecrpt(tt, key));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
