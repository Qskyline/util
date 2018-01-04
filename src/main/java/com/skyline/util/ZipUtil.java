package com.skyline.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class ZipUtil {
	public static boolean unZipFile(String zipPath, String fileName, String unZipPath) throws IOException {
		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath)), Charset.forName("GBK"));
		ZipFile zf = null;
		FileOutputStream fileOutputStream = null;
		ZipEntry ze = null;
        while ((ze = zin.getNextEntry()) != null) {
            if (!ze.isDirectory() && ze.getSize() > 0 && ze.getName().equals(fileName)) {
            	zf = new ZipFile(zipPath);
            	InputStream is = zf.getInputStream(ze);
            	
            	File path = new File(unZipPath);
            	if(!path.exists()) path.mkdirs();	            	
            	File file = new File(unZipPath + fileName);	            	
            	fileOutputStream = new FileOutputStream(file, false);
            	
            	byte[] buf = new byte[1024];
            	int len = -1;
            	while ((len = is.read(buf)) != -1) {
            		fileOutputStream.write(buf, 0, len);
            	}
            	
            	fileOutputStream.flush();
            	fileOutputStream.close();
            	zf.close();
            	zin.close();
            	return true;
            }
        }
        zin.close();
		return false;
	}
	
	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
}
