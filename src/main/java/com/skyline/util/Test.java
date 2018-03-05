package com.skyline.util;

import java.security.NoSuchAlgorithmException;
import net.sf.json.JSONObject;

public class Test {
	 public static void main( String[] args )
	 {
//		 NetworkUtil.tcpFileClientSend("127.0.0.1", 6666, 11023, "D:/kingdeecloudbackup/qwe/agent.zip");
	 
		 String ip = "172.20.70.100";
         JSONObject json = new JSONObject();
         long timestamp = System.currentTimeMillis();
         String sign = null;
         try {
        	 sign = SecurityUtil.Md5(json.toString() + "%$dasd?QW%^&d45&df" + timestamp);
         } catch (NoSuchAlgorithmException e) {
        	 // TODO Auto-generated catch block
        	 e.printStackTrace();
         }
         JSONObject data = new JSONObject();
         data.put("timestamp", timestamp);
         data.put("sign", sign);
         data.put("data", json.toString());

         String filePath = "D:/kingdeecloudbackup/qwe/agent.zip";
         int result = NetworkUtil.tcpFileClientSend(ip, 6666, 8888, filePath, data.toString());
         System.out.println(result);
	 }
}
