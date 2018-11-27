package com.skyline.util;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarInputStream;
import org.apache.tools.tar.TarOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressUtil {
	static Logger logger = LoggerFactory.getLogger(CompressUtil.class);

	public static boolean unZipFile(String zipPath, String unZipPath, String fileName) throws IOException {
		if (StringUtils.isBlank(zipPath) || StringUtils.isBlank(unZipPath)) {
			logger.error("the param zipPath and unZipPath can not be null.");
			return false;
		}
		File zipPathFile = new File(zipPath);
		if (!zipPathFile.isFile()) {
			logger.error("the param zipPath must be a file.");
			return false;
		}
		File unZipPathFile = new File(unZipPath);
		if (!unZipPathFile.isDirectory()) {
			logger.error("the param unZipPath must be a directory.");
			return false;
		} else if (!unZipPathFile.exists()) {
			unZipPathFile.mkdirs();
		}

		boolean isSingleFileExtract = StringUtils.isNotBlank(fileName);

		ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipPath)), Charset.forName("GBK"));
		ZipFile zf = new ZipFile(zipPath);
		FileOutputStream fileOutputStream;
		ZipEntry ze;
        while ((ze = zin.getNextEntry()) != null) {
			if (ze.isDirectory() && StringUtils.isNotBlank(ze.getName()) && !isSingleFileExtract) {
				new File(unZipPath + ze.getName()).mkdirs();
			}

			if (!ze.isDirectory() && (!isSingleFileExtract || (isSingleFileExtract && ze.getName().equals(fileName)))) {
				InputStream is = zf.getInputStream(ze);

				File file = new File(unZipPath + ze.getName());
				if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
				fileOutputStream = new FileOutputStream(file, false);

				byte[] buf = new byte[2048];
				int len;
				while ((len = is.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, len);
				}
				fileOutputStream.flush();
				fileOutputStream.close();

				if (isSingleFileExtract) break;
			}
		}

		zf.close();
		zin.close();
		return true;
	}
	
	public static void zipCompress(File inputFile, ZipOutputStream zos, String dirPath) throws Exception {
		dirPath = (StringUtils.isBlank(dirPath) ? "" : (dirPath + File.separator)) + inputFile.getName();
		if (inputFile.isDirectory()) {
			File[] files = inputFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				zipCompress(files[i], zos, dirPath);
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
	
	public static void zipCompress(List<String> inputFilePaths, String desFilePath, String dirPath) throws Exception {
    	if(inputFilePaths == null) return; 
    	ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(desFilePath));
		for (String string : inputFilePaths) {
			File inputFile = new File(string);
			zipCompress(inputFile, zos, dirPath);
		}
    	zos.close();
	}
	
	public static void zipCompress(List<String> inputFilePaths, String desFilePath) throws Exception {
		zipCompress(inputFilePaths, desFilePath, null);
	}
	
	public static void zipCompress(String inputFilePath, String desFilePath, String dirPath) throws Exception {
		ArrayList<String> temp = new ArrayList<>();
		temp.add(inputFilePath);
		zipCompress(temp, desFilePath, dirPath);
	}
	
	public static void zipCompress(String inputFilePath, String desFilePath) throws Exception {
		zipCompress(inputFilePath, desFilePath, null);
	}
	
	public static void zipCompress(String inputFilePath) throws Exception {
		zipCompress(inputFilePath, inputFilePath + ".zip");
	}

	public static boolean unTarGz(String tarGzPath, String outputDir, String fileName, boolean delAfterUnTar) throws IOException {
		if (StringUtils.isBlank(tarGzPath) || StringUtils.isBlank(outputDir)) {
			logger.error("the param can not be null.");
			return false;
		}
		File tarGzPathFile = new File(tarGzPath);
		if (!tarGzPathFile.isFile()) {
			logger.error("the param tarGzPath must be a file.");
			return false;
		}
		File outputDirFile = new File(outputDir);
		if (!outputDirFile.isDirectory()) {
			logger.error("the param outputDir must be a directory.");
			return false;
		} else if (!outputDirFile.exists()) {
			outputDirFile.mkdirs();
		}

		boolean isSingleFileExtract = StringUtils.isNotBlank(fileName);

		TarInputStream tarIn = new TarInputStream(new GZIPInputStream(new BufferedInputStream(new FileInputStream(tarGzPathFile))), 1024 * 2);
		TarEntry entry;
		while ((entry = tarIn.getNextEntry()) != null) {
			if (entry.isDirectory() && !StringUtils.isBlank(entry.getName()) && !isSingleFileExtract) {
				new File(outputDir + entry.getName()).mkdirs();
			}

			if(!entry.isDirectory() && (!isSingleFileExtract || (isSingleFileExtract && entry.getName().equals(fileName)))) {
				File tmpFile = new File(outputDir + entry.getName());
				if (!tmpFile.getParentFile().exists()) tmpFile.getParentFile().mkdirs();
				OutputStream out = new FileOutputStream(tmpFile);
				int length;
				byte[] b = new byte[2048];
				while((length = tarIn.read(b)) != -1) {
					out.write(b, 0, length);
				}
				out.flush();
				out.close();
				if (isSingleFileExtract) break;
			}
		}
		tarIn.close();
		if (delAfterUnTar) tarGzPathFile.deleteOnExit();
		return true;
	}

	public static boolean unTarGz(String tarGzPath, String outputDir, boolean delAfterUnTar) throws IOException {
		return unTarGz(tarGzPath, outputDir, null, delAfterUnTar);
	}

	public static boolean unTarGz(String tarGzPath, String outputDir) throws IOException {
		return unTarGz(tarGzPath, outputDir, null, false);
	}

	public static void tarGzCompress(File inputFile, TarOutputStream tos, String dirPath) throws Exception {
		dirPath = (StringUtils.isBlank(dirPath) ? "" : (dirPath + File.separator)) + inputFile.getName();
		if (inputFile.isDirectory()) {
			File[] files = inputFile.listFiles();
			for (int i = 0; i < files.length; i++) {
				tarGzCompress(files[i], tos, dirPath);
			}
		} else {
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inputFile));
			TarEntry tarEntry = new TarEntry(dirPath);
			tarEntry.setSize(inputFile.length());
			tos.putNextEntry(tarEntry);
			while (true) {
				byte[] b = new byte[10240];
				int len = bis.read(b);
				if (len == -1) {
					break;
				}
				tos.write(b, 0, len);
			}
			tos.closeEntry();
			tos.flush();
			bis.close();
		}
	}

	public static void tarGzCompress(File inputFile, String desFilePath, String dirPath) throws Exception {
		if(inputFile == null) return;
		TarOutputStream tos = new TarOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(desFilePath))));
		tarGzCompress(inputFile, tos, dirPath);
		tos.close();
	}

	public static void tarGzCompress(File inputFile, String desFilePath) throws Exception {
		tarGzCompress(inputFile, desFilePath, null);
	}

	public static void tarGzCompress(List<String> inputFilePaths, String desFilePath, String dirPath) throws Exception {
		if(inputFilePaths == null) return;
		TarOutputStream tos = new TarOutputStream(new GZIPOutputStream(new BufferedOutputStream(new FileOutputStream(desFilePath))));
		for (String string : inputFilePaths) {
			File inputFile = new File(string);
			tarGzCompress(inputFile, tos, dirPath);
		}
		tos.close();
	}

	public static void tarGzCompress(List<String> inputFilePaths, String desFilePath) throws Exception {
		tarGzCompress(inputFilePaths, desFilePath, null);
	}

	public static void tarGzCompress(String inputFilePath, String desFilePath, String dirPath) throws Exception {
		ArrayList<String> temp = new ArrayList<>();
		temp.add(inputFilePath);
		tarGzCompress(temp, desFilePath, dirPath);
	}

	public static void tarGzCompress(String inputFilePath, String desFilePath) throws Exception {
		tarGzCompress(inputFilePath, desFilePath, null);
	}

	public static void tarGzCompress(String inputFilePath) throws Exception {
		tarGzCompress(inputFilePath, inputFilePath + ".tar.gz");
	}

	public static void main(String[] args) {
		try {
//			unZipFile("/Users/skyline/Downloads/test.zip","/Users/skyline/Downloads/tt/", "");
			tarGzCompress("/Users/skyline/Downloads/test");
//			zipCompress("/Users/skyline/Downloads/test");

		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
