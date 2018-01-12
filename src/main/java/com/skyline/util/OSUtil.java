package com.skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class OSUtil {
	public static List<String> getLanIp() throws SocketException, UnknownHostException{
    	ArrayList<String> result = new ArrayList<String>();
    	ArrayList<String> candidateAddresses = new ArrayList<String>();
    	
        // 遍历所有的网络接口
        for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements(); ) {
            NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
            // 在所有的接口下再遍历IP
            for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements(); ) {
                InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
                if (inetAddr != null && !inetAddr.isLoopbackAddress() && inetAddr instanceof Inet4Address) {
                    if (inetAddr.isSiteLocalAddress()) {
                        result.add(inetAddr.getHostAddress());	                        
                    } else {
                        candidateAddresses.add(inetAddr.getHostAddress());
                    }
                }
            }
        }
        
        if (result.size() != 0) {
            return result;
        } else if(candidateAddresses.size() != 0) {
        	return candidateAddresses;
        } else {
        	result.add(InetAddress.getLocalHost().getHostAddress());
        	return result;
        }
	}
	
	public static double getDiskFreeSpace(String path) {
		File diskPartition = new File(path);
		if(!diskPartition.isDirectory()) return -1;
		return ((double)diskPartition.getFreeSpace())/1024/1024/1024;
	}
	
	public static String execShell(String ip, String user, String password, String shell) {
		String  DEFAULTCHART="UTF-8";
		String result = null;
		try {
			Connection conn = new Connection(ip); 
            conn.connect();
            if(conn.authenticateWithPassword(user, password)) {
            	Session session = conn.openSession();
				session.execCommand(shell);
				result = processStdout(session.getStdout(), DEFAULTCHART);
				if(StringUtils.isBlank(result)){
					result=processStdout(session.getStderr(), DEFAULTCHART);  
		        }
				session.close(); 
            }
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;		
	}
	
	private static String processStdout(InputStream in, String charset) {
		InputStream stdout = new StreamGobbler(in);
		StringBuffer buffer = new StringBuffer();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(stdout, charset));
			String line = null;
			while ((line = br.readLine()) != null) {
				buffer.append(line + "\n");
			}
			br.close();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static void main(String[] args) {
		System.out.println(execShell("54.222.241.235", "awsskyline", "Eas@2016", "pwd"));
	}
}
