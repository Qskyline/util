package com.skyline.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.StringUtils;

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
	
	public static void compress(File inputFile, ZipOutputStream zos, String dirPath) throws Exception {
		dirPath = (StringUtils.isBlank(dirPath) ? "" : (dirPath + File.separator)) + inputFile.getName();
		if (inputFile.isDirectory()) {
			File[] files = inputFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				compress(files[i], zos, dirPath);
			}
		} else {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
			zos.putNextEntry(new ZipEntry(dirPath));
			while (true) {
				byte[] b = new byte[10240];
				int len = bis.read(b);
				if (len == -1) {
					break;
				}
				zos.write(b, 0, len);
			}
			bis.close();
		}
	}
	
	public static void compress(List<String> inputFilePaths, String desFilePath, String dirPath) throws Exception {
    	if(inputFilePaths == null) return; 
    	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(desFilePath));
		for (String string : inputFilePaths) {
			File inputFile = new File(string);
			compress(inputFile, zos, dirPath);
		}
    	zos.close();
	}
	
	public static void compress(List<String> inputFilePaths, String desFilePath) throws Exception {
		compress(inputFilePaths, desFilePath, null);
	}
	
	public static void compress(String inputFilePath, String desFilePath, String dirPath) throws Exception {
		ArrayList<String> temp = new ArrayList<>();
		temp.add(inputFilePath);
		compress(temp, desFilePath, dirPath);
	}
	
	public static void compress(String inputFilePath, String desFilePath) throws Exception {
		compress(inputFilePath, desFilePath, null);
	}
	
	public static void compress(String inputFilePath) throws Exception {
		compress(inputFilePath, inputFilePath + ".zip");
	}
	
	public static void main(String[] args) throws Exception {
		ArrayList<String> t = new ArrayList<>();
		t.add("C:/Users/skyline/Desktop/temp/catalina.out");
		t.add("C:/Users/skyline/Desktop/temp/20171226");		
		compress(t, "C:/Users/skyline/Desktop/temp/20171226.zip");
		System.out.println("Hello World!");
	}
}
