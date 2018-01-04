package com.skyline.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import net.sf.json.JSONObject;

public class HttpUtil 
{
	public static String sendPost(String url, String params) throws ClientProtocolException, IOException {
		String result = null;
		
		String[] param = params.split("&");
		List<BasicNameValuePair> formparams = new ArrayList<BasicNameValuePair>();		
		for (String string : param) {
			String[] pair = string.split("=");
			if(pair.length != 2) continue;
			formparams.add(new BasicNameValuePair(pair[0], pair[1]));
		}
		UrlEncodedFormEntity uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");  
		
		HttpPost httppost = new HttpPost(url);
		RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(50000).setConnectionRequestTimeout(10000)
                .setSocketTimeout(50000).build();
		httppost.setConfig(requestConfig);
		httppost.setEntity(uefEntity);
		
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse response = httpClient.execute(httppost); 
		
        HttpEntity entity = response.getEntity();
        if (entity != null) {  
        	result = EntityUtils.toString(entity, "UTF-8");  
        }
        
		if(response != null) response.close();
    	if(httpClient != null) httpClient.close();
    	
		return result;
	}
	
	public static int fetchPostRequestStatus(String _url, String params) throws IOException {
		URL url = new URL(_url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent", "Chrome/61.0.3163.100");
		conn.setDoOutput(true);  
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        if(params != null) out.print(params);
        out.flush();
		return conn.getResponseCode();
	}
	
	public static int fetchGetRequestStatus(String _url) throws IOException {
		URL url = new URL(_url);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();  
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("user-agent", "Chrome/61.0.3163.100");
		conn.connect();
		return conn.getResponseCode();
	}
	
	public static boolean download(String _url, String params, String filePath, JSONObject json) throws IOException {
		URL url = new URL(_url);
		URLConnection conn = url.openConnection();
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setDoOutput(true);
        conn.setDoInput(true);
        PrintWriter out = new PrintWriter(conn.getOutputStream());
        out.print(params);
        out.flush();
        
        Map<String, List<String>> _headers = conn.getHeaderFields();
        JSONObject headers = new JSONObject();
        if(_headers != null) {
        	for (String key: _headers.keySet()) {
        		if(key != null) {
        			headers.put(key, _headers.get(key));
        		}
			}
        }
        
        json.put("headers", headers);
		String contentType = conn.getContentType();
		if (StringUtils.isBlank(contentType)) {
			conn = null;
			return false;
		}
		
		if (contentType.toLowerCase().contains("application/json")) {
			String result = "";
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"utf-8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            in.close();
            if(result != null && !result.equals("")) {
            	json.put("data", result);
            }
            return false;
		} else if (contentType.toLowerCase().contains("application/octet-stream")) {
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(new File(filePath),false)));
			DataInputStream dis = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
			byte[] _buff = new byte[2048];
			int len = 0;
			while((len = dis.read(_buff)) >= 0){
				dos.write(_buff, 0, len);
			}
			dis.close();
			dos.close();
			return true;
		}
		return false;
	}
	
	public static void writeToResponse(HttpServletResponse response, Object responseModel) throws IOException {
		if (response == null) return;
		String res = "";
		if(responseModel != null) res = JSONObject.fromObject(responseModel).toString();		
		PrintWriter writer = response.getWriter();
		writer.write(res);
		writer.flush();
		writer.close();
	}
	
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }
}
