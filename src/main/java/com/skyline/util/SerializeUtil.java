package com.skyline.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeUtil {
	public static boolean serializeObject(String path, Object o) throws IOException {
		if(path == null || o == null) return false;
		File file = new File(path);
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if(!file.exists()) file.createNewFile();
		fos = new FileOutputStream(file);
		oos = new ObjectOutputStream(fos);
		oos.writeObject(o);  
		oos.flush();
		oos.close(); 
		fos.close();
		return true;
	}
	
	public static <T> T deserializeObject(String path) throws IOException, ClassNotFoundException {
		if(path == null) return null;
		File file = new File(path);
		if(!file.exists()) return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		fis = new FileInputStream(file);
		ois = new ObjectInputStream(fis);
		@SuppressWarnings("unchecked")
		T pdm = (T) ois.readObject();
		ois.close();
		fis.close();
		return pdm;
	}
	
	public static void main(String[] args) {
		System.out.println("Hello World!");
	}
}
