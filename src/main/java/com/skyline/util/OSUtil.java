package com.skyline.util;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
	
	public static void main(String[] args) {
		try {
			System.out.println(getLanIp());
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
